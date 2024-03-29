package com.wcaokaze.ninja60.parts.rotaryencoder.gear

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.ninja60.shared.scadutil.*
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
) : TransformableDefaultImpl<Gear> {
   companion object {
      val PROFILE_ANGLE = 20.deg

      fun toothCount(module: Size, diameter: Size): Int
            = ((diameter - module * 2) / module).toInt()
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = Gear(module, toothCount, thickness, referencePoint, frontVector, bottomVector)
}

/**
 * 中心距離。[Gear.referencePoint]には関係なく
 * この2つの歯車が配置されるべき理想的な中心距離を返す。
 */
infix fun Gear.idealDistance(another: Gear): Size {
   require(module == another.module) {
      "Attempt to get the distance even though their modules unmatching"
   }

   return module * (toothCount + another.toothCount) / 2
}

fun ScadParentObject.gear(gear: Gear): ScadObject {
   return place(gear) {
      gearAtOrigin(gear)
   }
}

/**
 * [位置][Gear.referencePoint]と[向き][Gear.frontVector]を無視して
 * gearをXY平面の原点に配置する。
 *
 * [place]を別途自力で呼び出した方が都合がいい場合にどうぞ
 */
fun ScadParentObject.gearAtOrigin(gear: Gear): ScadObject {
   return intersection {
      cylinder(gear.thickness, gear.addendumDiameter / 2)
      union {
         cylinder(gear.thickness, gear.bottomDiameter / 2)

         repeatRotation(gear.toothCount) {
            tooth(gear)
         }
      }
   }
}

/** 標準円直径 */
val Gear.pitchDiameter: Size get() = module * toothCount
/** 標準円半径 */
val Gear.pitchRadius: Size get() = pitchDiameter / 2
/** 基礎円直径 */
val Gear.involuteDiameter: Size get() = pitchDiameter * cos(Gear.PROFILE_ANGLE)
/** 基礎円半径 */
val Gear.involuteRadius: Size get() = involuteDiameter / 2
/** 歯先円直径 */
val Gear.addendumDiameter: Size get() = pitchDiameter + module * 2
/** 歯先円半径 */
val Gear.addendumRadius: Size get() = addendumDiameter / 2
/** 歯底円直径 */
val Gear.bottomDiameter: Size get() = pitchDiameter - module * 2.5
/** 歯底円半径 */
val Gear.bottomRadius: Size get() = bottomDiameter / 2

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

   return linearExtrude(gear.thickness) {
      polygon(
         (0.0.rad..Angle.PI step fa.value)
            .asSequence()
            .map { a ->
               Point2d(
                  Point(gear.involuteRadius * (cos(a) + a.numberAsRadian * sin(a))),
                  Point(gear.involuteRadius * (sin(a) - a.numberAsRadian * cos(a)))
               )
            }
            .map { it.rotate(Point2d.ORIGIN, -involuteHalfThicknessAngle) }
            .takeWhile { it.y < Point.ORIGIN }
            .toList()
            .let { halfPoints ->
               halfPoints +
               halfPoints.asReversed().map(Point2d::mirror) +
               Point2d.ORIGIN
            }
      )
   }
}

internal fun Point2d.mirror() = Point2d(x, Point(-y.distanceFromOrigin))
