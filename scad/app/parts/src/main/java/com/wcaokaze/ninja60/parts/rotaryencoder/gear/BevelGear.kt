package com.wcaokaze.ninja60.parts.rotaryencoder.gear

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.ninja60.shared.scadutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*
import kotlin.math.*

/**
 * 標準すぐばかさ歯車。
 *
 * 平歯車と違い噛み合う条件が非常に複雑なので[createPair]で生成するのがおすすめ
 */
data class BevelGear(
   val module: Size,
   val toothCount: Int,
   val toothThickness: Size,
   val pitchConeAngle: Angle,
   override val referencePoint: Point3d,
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d
) : Transformable<BevelGear> {
   companion object {
      val PROFILE_ANGLE = 20.deg

      /**
       * ふたつの噛み合うBevelGearを生成する。
       *
       * @return
       * [Pair.first]に歯数[toothCountA]で[原点][Point3d.ORIGIN]に配置された歯車、
       * [Pair.second]に歯数[toothCountB]でXZ平面上の[Pair.first]と噛み合う位置に配置された歯車
       */
      fun createPair(
         module: Size,
         operatingAngle: Angle,
         toothCountA: Int,
         toothCountB: Int,
         toothThickness: Size
      ): Pair<BevelGear, BevelGear> {
         var gearA = BevelGear(
            module, toothCountA, toothThickness,
            pitchConeAngle = Angle(atan(
               sin(operatingAngle) /
                     (toothCountB.toDouble() / toothCountA.toDouble() + cos(operatingAngle))
            )),
            Point3d.ORIGIN, -Vector3d.Y_UNIT_VECTOR, -Vector3d.Z_UNIT_VECTOR
         )

         var gearB = BevelGear(
            module, toothCountB, toothThickness,
            pitchConeAngle = operatingAngle - gearA.pitchConeAngle,
            Point3d.ORIGIN, -Vector3d.Y_UNIT_VECTOR, -Vector3d.Z_UNIT_VECTOR
         )

         val fixedToothThickness = toothThickness
            .coerceAtMost(gearB.outerConeDistance / 3)
            .coerceAtMost(module * 10)
         gearA = gearA.copy(toothThickness = fixedToothThickness)
         gearB = gearB.copy(toothThickness = fixedToothThickness)

         gearB = gearB.translate(
            z = gearA.outerDedendumConeHeight - gearB.outerDedendumConeHeight)

         gearB = gearB.rotate(
            Line3d(gearB.axisIntersectionPoint, -Vector3d.Y_UNIT_VECTOR),
            operatingAngle
         )

         return Pair(gearA, gearB)
      }
   }

   /**
    * 2つの噛み合う歯車の軸の交点。
    *
    * この点を中心として[回転移動][Transformable.rotate]する限り
    * 2つの歯車は噛み合ったまま移動できる。
    */
   val axisIntersectionPoint: Point3d
      get() = referencePoint.translate(topVector, outerDedendumConeHeight)

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = BevelGear(module, toothCount, toothThickness, pitchConeAngle, referencePoint, frontVector, bottomVector)
}

val BevelGear.outerConeDistance: Size get() = module * toothCount / 2 / sin(pitchConeAngle)
val BevelGear.innerConeDistance: Size get() = outerConeDistance - toothThickness
val BevelGear.outerAddendum: Size get() = module * 1.25
val BevelGear.outerDedendum: Size get() = module
val BevelGear.addendumAngle: Angle get() = atan(outerAddendum, outerConeDistance)
val BevelGear.dedendumAngle: Angle get() = atan(outerDedendum, outerConeDistance)
val BevelGear.innerAddendum: Size get() = innerConeDistance * tan(addendumAngle)
val BevelGear.innerDedendum: Size get() = innerConeDistance * tan(dedendumAngle)
val BevelGear.faceConeAngle: Angle get() = pitchConeAngle + addendumAngle
val BevelGear.rootConeAngle: Angle get() = pitchConeAngle - dedendumAngle

private val BevelGear.outerAddendumConeHeight: Size
   get() = outerConeDistance * cos(pitchConeAngle) - outerAddendum * sin(pitchConeAngle)
private val BevelGear.outerDedendumConeHeight: Size
   get() = outerConeDistance * cos(pitchConeAngle) + outerDedendum * sin(pitchConeAngle)
private val BevelGear.innerAddendumConeHeight: Size
   get() = innerConeDistance * cos(pitchConeAngle) - innerAddendum * sin(pitchConeAngle)
private val BevelGear.innerDedendumConeHeight: Size
   get() = innerConeDistance * cos(pitchConeAngle) + innerDedendum * sin(pitchConeAngle)
private val BevelGear.outerAddendumConeRadius: Size
   get() = outerConeDistance * sin(pitchConeAngle) + outerAddendum * cos(pitchConeAngle)
private val BevelGear.outerDedendumConeRadius: Size
   get() = outerConeDistance * sin(pitchConeAngle) - outerDedendum * cos(pitchConeAngle)
private val BevelGear.innerAddendumConeRadius: Size
   get() = innerConeDistance * sin(pitchConeAngle) + innerAddendum * cos(pitchConeAngle)
private val BevelGear.innerDedendumConeRadius: Size
   get() = innerConeDistance * sin(pitchConeAngle) - innerDedendum * cos(pitchConeAngle)

fun ScadParentObject.bevelGear(bevelGear: BevelGear): ScadObject {
   return place(bevelGear) {
      intersection {
         cylinder(
            bevelGear.outerDedendumConeHeight,
            bottomRadius = bevelGear.outerDedendumConeHeight * tan(bevelGear.faceConeAngle),
            topRadius = 0.mm
         )

         union {
            cylinder(
               bevelGear.outerDedendumConeHeight - bevelGear.innerDedendumConeHeight,
               bottomRadius = bevelGear.outerDedendumConeRadius,
               topRadius = bevelGear.innerDedendumConeRadius
            )

            repeatRotation(bevelGear.toothCount) {
               tooth(bevelGear)
            }
         }
      }
   }
}

private fun ScadParentObject.tooth(gear: BevelGear): ScadObject {
   // 歯から次の歯までの角度
   val pitchAngle = Angle.PI * 2 / gear.toothCount
   // 標準円上での歯1枚の角度
   val thicknessAngle = pitchAngle / 2
   // 標準円上での歯の半分の角度
   val halfThicknessAngle = thicknessAngle / 2
   // 基礎円上での歯の半分の角度
   val involuteHalfThicknessAngle = halfThicknessAngle +
         tan(BevelGear.PROFILE_ANGLE).rad - BevelGear.PROFILE_ANGLE

   /**
    * 激薄の歯を生成する。
    *
    * @param distance
    * 円錐の頂点から歯までの距離。ここでいう歯の位置は標準円錐上にある点のことを指し、
    * Z軸上とかではないです
    */
   fun ScadParentObject.thinTooth(distance: Size): ScadObject {
      val pitchRadius = distance * tan(gear.pitchConeAngle)
      val involuteRadius = pitchRadius * cos(BevelGear.PROFILE_ANGLE)

      return linearExtrude(0.01.mm) {
         polygon(
            (0.0.rad..Angle.PI / 2 step fa.value)
               .map { a ->
                  Point2d(
                     Point(involuteRadius * (cos(a) + a.numberAsRadian * sin(a))),
                     Point(involuteRadius * (sin(a) - a.numberAsRadian * cos(a)))
                  )
               }
               + Point2d.ORIGIN
         )
      }
   }

   return intersection {
      val half = rotate(z = -involuteHalfThicknessAngle) {
         (gear.innerConeDistance..gear.outerConeDistance step fs.value)
            .plus(gear.outerConeDistance)
            .zipWithNext()
            .forEach { (a, b) ->
               hull {
                  for (distance in listOf(a, b)) {
                     translate(z = gear.outerDedendumConeHeight
                           - distance / cos(gear.pitchConeAngle))
                     {
                        rotate(y = -gear.pitchConeAngle) {
                           thinTooth(distance)
                        }
                     }
                  }
               }
            }
      }

      mirror(0.mm, 1.mm, 0.mm) {
         addChild(half)
      }
   }
}
