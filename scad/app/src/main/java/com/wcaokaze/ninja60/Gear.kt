package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * インボリュート歯車の平歯車です
 */
data class Gear(
   val module: Int,
   val toothCount: Int,
   val thickness: Size,
   override val referencePoint: Point3d,
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d
) : Transformable<Gear> {
   companion object {
      val PRESSURE_ANGLE = 20.deg
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = Gear(module, toothCount, thickness, referencePoint, frontVector, bottomVector)
}

fun ScadParentObject.gear(gear: Gear): ScadObject {
   val addendumDiameter = gear.diameter + (gear.module).mm * 2.0
   val bottomDiameter = gear.diameter - (gear.module).mm * 2.5

   return (
      union {
         for (i in 0 until gear.toothCount) {
            tooth(gear).rotate(z = 360.deg / gear.toothCount * i)
         }
      }
      + cylinder(gear.thickness, bottomDiameter / 2, `$fa`)
      intersection cylinder(gear.thickness, addendumDiameter / 2, `$fa`)
   )
}

private val Gear.diameter get() = module.mm * toothCount
private val Gear.radius get() = diameter / 2
private val Gear.involuteDiameter get() = diameter * cos(Gear.PRESSURE_ANGLE)
private val Gear.involuteRadius get() = involuteDiameter / 2

private fun ScadParentObject.tooth(gear: Gear): ScadObject {
   val pitchAngle = Angle.PI * 2 / gear.toothCount

   return intersection {
      val half = linearExtrude(gear.thickness) {
         // FIXME: 歯厚計算されてません
         rotate(z = -pitchAngle * 0.283) {
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
