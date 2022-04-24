package com.wcaokaze.ninja60.parts.rotaryencoder.left

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.parts.rotaryencoder.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

class LeftInnerRotaryEncoderKnob(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : Transformable<LeftInnerRotaryEncoderKnob> {
   companion object {
      val RADIUS = 10.mm
      val HEIGHT = 13.mm
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
      = LeftInnerRotaryEncoderKnob(frontVector, bottomVector, referencePoint)

   val rotaryEncoder: RotaryEncoder get() {
      val outerKnob = LeftOuterRotaryEncoderKnob(
         frontVector, bottomVector,
         referencePoint.translate(bottomVector, LeftOuterRotaryEncoderKnob.HEIGHT)
      )

      val gearBottomPlane = Plane3d(outerKnob.gear.referencePoint, topVector)

      val gearBottomPoint = gearBottomPlane intersection Line3d(referencePoint, topVector)

      return RotaryEncoder(
         frontVector, bottomVector,
         gearBottomPoint.translate(bottomVector, RotaryEncoder.BODY_SIZE.z + 1.mm)
      )
   }
}

fun ScadParentObject.leftInnerRotaryEncoderKnob(
   leftInnerRotaryEncoderKnob: LeftInnerRotaryEncoderKnob
): ScadObject {
   return (
      place(leftInnerRotaryEncoderKnob) {
         cylinder(
            LeftInnerRotaryEncoderKnob.HEIGHT,
            LeftInnerRotaryEncoderKnob.RADIUS
         )
      }
      - rotaryEncoderKnobHole(leftInnerRotaryEncoderKnob.rotaryEncoder)
   )
}
