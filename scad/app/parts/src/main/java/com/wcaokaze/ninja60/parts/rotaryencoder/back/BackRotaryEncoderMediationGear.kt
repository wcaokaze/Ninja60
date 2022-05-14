package com.wcaokaze.ninja60.parts.rotaryencoder.back

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.parts.rotaryencoder.gear.*
import com.wcaokaze.ninja60.parts.rotaryencoder.gear.BevelGear as Bevel
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

data class BackRotaryEncoderMediationGear(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : TransformableDefaultImpl<BackRotaryEncoderMediationGear> {
   object SpurGear {
      val MODULE = 1.5.mm
      val TOOTH_COUNT = 17
      val THICKNESS = 2.mm
   }

   object BevelGear {
      val MODULE = 1.5.mm
      val OPERATING_ANGLE = 90.deg
      val TOOTH_COUNT = 11
      val THICKNESS = 4.mm

      fun createPair(): Pair<Bevel, Bevel> {
         return Bevel.createPair(
            MODULE,
            OPERATING_ANGLE,
            TOOTH_COUNT,
            BackRotaryEncoderGear.Gear.TOOTH_COUNT,
            THICKNESS
         )
      }
   }

   companion object {
      val SHAFT_HOLE_RADIUS = 1.1.mm
   }

   val spurGear get() = Gear(
      SpurGear.MODULE,
      SpurGear.TOOTH_COUNT,
      SpurGear.THICKNESS,
      referencePoint,
      frontVector,
      bottomVector
   )

   val bevelGear: Bevel get() {
      val (bevelGear, _) = BevelGear.createPair()
      return bevelGear.copy(
         referencePoint.translate(topVector, SpurGear.THICKNESS),
         frontVector,
         bottomVector
      )
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = BackRotaryEncoderMediationGear(frontVector, bottomVector, referencePoint)
}

fun ScadParentObject.backRotaryEncoderMediationGear(gear: BackRotaryEncoderMediationGear): ScadObject {
   return (
      gear(gear.spurGear)
      + bevelGear(gear.bevelGear)
      - place(gear) {
         cylinder(
            BackRotaryEncoderKnob.HEIGHT * 3,
            BackRotaryEncoderMediationGear.SHAFT_HOLE_RADIUS,
            center = true
         )
      }
   )
}
