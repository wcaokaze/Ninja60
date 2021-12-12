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
