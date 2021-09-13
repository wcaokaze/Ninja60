package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * インボリュート歯車の平歯車です
 */
data class Gear(
   val module: Size,
   val toothCount: Int,
   val thickness: Size,
   override val referencePoint: Point3d,
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d
) : Transformable<Gear> {
   companion object {
      val PROFILE_ANGLE = 20.deg
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = Gear(module, toothCount, thickness, referencePoint, frontVector, bottomVector)
}

infix fun Gear.distance(another: Gear): Size {
   require(module == another.module) {
      "Attempt to get the distance even though their modules unmatching"
   }

   return module * (toothCount + another.toothCount) / 2
}

fun ScadParentObject.gear(gear: Gear): ScadObject {
   val addendumDiameter = gear.diameter + gear.module * 2.0
   val bottomDiameter = gear.diameter - gear.module * 2.5

   return locale(gear.referencePoint) {
      rotate(
         -Vector3d.Z_UNIT_VECTOR angleWith gear.bottomVector,
         -Vector3d.Z_UNIT_VECTOR vectorProduct gear.bottomVector,
      ) {
         (
            union {
               for (i in 0 until gear.toothCount) {
                  tooth(gear).rotate(z = 360.deg / gear.toothCount * i)
               }
            }
            + cylinder(gear.thickness, bottomDiameter / 2, `$fa`)
            intersection cylinder(gear.thickness, addendumDiameter / 2, `$fa`)
         )
      }
   }
}

private val Gear.diameter get() = module * toothCount
private val Gear.radius get() = diameter / 2
private val Gear.involuteDiameter get() = diameter * cos(Gear.PROFILE_ANGLE)
private val Gear.involuteRadius get() = involuteDiameter / 2

private fun ScadParentObject.tooth(gear: Gear): ScadObject {
   /** 歯から次の歯までの角度 */
   val pitchAngle = Angle.PI * 2 / gear.toothCount
   /** 標準円上での歯1枚の角度 */
   val thicknessAngle = pitchAngle / 2
   /** 標準円上での歯の半分の角度 */
   val halfThicknessAngle = thicknessAngle / 2
   /**
    * 基礎円上での歯の半分の角度。
    * 歯が直線ではなくインボリュート曲線のため[pitchThicknessAngle]より少し広い
    * ちなみにこの角度を算出するtan(α) - αのことをインボリュート関数と呼ぶらしいです
    */
   val involuteHalfThicknessAngle = halfThicknessAngle + tan(Gear.PROFILE_ANGLE).rad - Gear.PROFILE_ANGLE

   return intersection {
      val half = linearExtrude(gear.thickness) {
         rotate(z = -involuteHalfThicknessAngle) {
            polygon(
               (0.0.rad..Angle.PI / 2 step (Angle.PI * 2 / `$fa`))
                  .map { a ->
                     Point2d(
                        Point(gear.involuteRadius * (cos(a) + a.numberAsRadian * sin(a))),
                        Point(gear.involuteRadius * (sin(a) - a.numberAsRadian * cos(a)))
                     )
                  }
                  + Point2d.ORIGIN
            )
         }
      }

      mirror(0.mm, 1.mm, 0.mm) {
         addChild(half)
      }
   }
}
