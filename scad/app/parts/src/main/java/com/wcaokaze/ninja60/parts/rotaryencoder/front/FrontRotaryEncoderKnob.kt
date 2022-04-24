package com.wcaokaze.ninja60.parts.rotaryencoder.front

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.parts.rotaryencoder.*
import com.wcaokaze.ninja60.shared.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

class FrontRotaryEncoderKnob(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : Transformable<FrontRotaryEncoderKnob> {
   companion object {
      val RADIUS = 18.mm
      val HEIGHT = 14.mm
      val HOLE_HEIGHT = HEIGHT - 2.mm
   }

   val rotaryEncoder get() = RotaryEncoder(
      frontVector, bottomVector,
      referencePoint.translate(topVector, HOLE_HEIGHT - RotaryEncoder.HEIGHT)
   )

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = FrontRotaryEncoderKnob(frontVector, bottomVector, referencePoint)
}

fun ScadParentObject.frontRotaryEncoderKnob(knob: FrontRotaryEncoderKnob): ScadObject {
   return (
      place(knob) {
         cylinder(
            FrontRotaryEncoderKnob.HEIGHT - PrinterAdjustments.errorSize.value,
            FrontRotaryEncoderKnob.RADIUS - PrinterAdjustments.errorSize.value)
      }
      - rotaryEncoderKnobHole(knob.rotaryEncoder)
   )
}
