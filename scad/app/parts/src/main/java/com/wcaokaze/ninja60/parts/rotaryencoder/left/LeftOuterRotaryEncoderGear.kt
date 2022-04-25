package com.wcaokaze.ninja60.parts.rotaryencoder.left

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.parts.rotaryencoder.*
import com.wcaokaze.ninja60.parts.rotaryencoder.gear.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

class LeftOuterRotaryEncoderGear(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : Transformable<LeftOuterRotaryEncoderGear> {
   companion object {
      val HEIGHT = 4.mm

      val RADIUS = (
            LeftOuterRotaryEncoderKnob.RADIUS
            - LeftOuterRotaryEncoderKnob.THICKNESS
            - RotaryEncoder.SHAFT_RADIUS
         ) / 2 - 0.5.mm

      val TOOTH_COUNT = Gear.toothCount(
         LeftOuterRotaryEncoderKnob.MODULE,
         RADIUS * 2
      )
   }

   val gear get() = Gear(
      LeftOuterRotaryEncoderKnob.MODULE,
      TOOTH_COUNT,
      HEIGHT,
      referencePoint,
      frontVector, bottomVector
   )

   val rotaryEncoder get() = RotaryEncoder(
      frontVector,
      bottomVector,
      referencePoint.translate(topVector, HEIGHT - RotaryEncoder.HEIGHT)
   )

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = LeftOuterRotaryEncoderGear(frontVector, bottomVector, referencePoint)
}

fun ScadParentObject.leftOuterRotaryEncoderGear(
   leftOuterRotaryEncoderGear: LeftOuterRotaryEncoderGear
): ScadObject {
   return difference {
      gear(leftOuterRotaryEncoderGear.gear)
      rotaryEncoderKnobHole(leftOuterRotaryEncoderGear.rotaryEncoder)
   }
}
