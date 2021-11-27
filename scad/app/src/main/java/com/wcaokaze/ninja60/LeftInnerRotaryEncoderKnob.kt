package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
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

      operator fun invoke(outerKnob: LeftOuterRotaryEncoderKnob) = LeftInnerRotaryEncoderKnob(
         outerKnob.frontVector,
         outerKnob.bottomVector,
         outerKnob.referencePoint
            .translate(outerKnob.topVector, LeftOuterRotaryEncoderKnob.HEIGHT)
      )
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
      = LeftInnerRotaryEncoderKnob(frontVector, bottomVector, referencePoint)
}

fun ScadParentObject.leftInnerRotaryEncoderKnob(
   leftInnerRotaryEncoderKnob: LeftInnerRotaryEncoderKnob
): ScadObject {
   return (
      locale(leftInnerRotaryEncoderKnob.referencePoint) {
         rotate(
            -Vector3d.Z_UNIT_VECTOR angleWith leftInnerRotaryEncoderKnob.bottomVector,
            -Vector3d.Z_UNIT_VECTOR vectorProduct leftInnerRotaryEncoderKnob.bottomVector
         ) {
            cylinder(
               LeftInnerRotaryEncoderKnob.HEIGHT,
               LeftInnerRotaryEncoderKnob.RADIUS,
               `$fa`
            )
         }
      }
   )
}
