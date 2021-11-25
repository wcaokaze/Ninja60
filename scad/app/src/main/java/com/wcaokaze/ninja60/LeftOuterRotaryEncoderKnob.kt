package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

class LeftOuterRotaryEncoderKnob(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : Transformable<LeftOuterRotaryEncoderKnob> {
   companion object {
      /** 奥から何番目の[KeySwitch]にノブを配置するか */
      val ROW_INDEX = 2

      val MODULE = 1.mm
      val RADIUS = 30.mm
      val HEIGHT = 14.mm
      val THICKNESS = 2.mm

      val INTERNAL_GEAR_TOOTH_COUNT = (
         ((RADIUS * 2 - THICKNESS) - MODULE * 2).numberAsMilliMeter
               / MODULE.numberAsMilliMeter
         ).toInt()

      operator fun invoke(alphanumericPlate: AlphanumericPlate): LeftOuterRotaryEncoderKnob {
         val leftmostColumn = alphanumericPlate.columns.first()
         val keySwitch = leftmostColumn.keySwitches[ROW_INDEX]

         val keycapTop = keySwitch.referencePoint
            .translate(keySwitch.topVector,
               KeySwitch.TOP_HEIGHT + KeySwitch.STEM_HEIGHT + Keycap.THICKNESS)

         val tangencyPoint = alphanumericPlate.leftmostPlane intersection
               Line3d(keycapTop, keySwitch.leftVector)

         return LeftOuterRotaryEncoderKnob(
            Vector3d.Y_UNIT_VECTOR,
            -Vector3d.Z_UNIT_VECTOR,
            tangencyPoint
               .translate(-Vector3d.X_UNIT_VECTOR, RADIUS + 2.mm)
               .translate( Vector3d.Y_UNIT_VECTOR, keyPitch.y / 3)
               .translate(-Vector3d.Z_UNIT_VECTOR, HEIGHT)
         )
      }
   }

   val internalGear get() = InternalGear(
      MODULE, INTERNAL_GEAR_TOOTH_COUNT, HEIGHT,
      referencePoint.translate(bottomVector, THICKNESS),
      frontVector, bottomVector
   )

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
      =  LeftOuterRotaryEncoderKnob(frontVector, bottomVector, referencePoint)
}

fun ScadParentObject.leftOuterRotaryEncoderKnob(
   leftOuterRotaryEncoderKnob: LeftOuterRotaryEncoderKnob
): ScadObject {
   return (
      locale(leftOuterRotaryEncoderKnob.referencePoint) {
         rotate(
            -Vector3d.Z_UNIT_VECTOR angleWith leftOuterRotaryEncoderKnob.bottomVector,
            -Vector3d.Z_UNIT_VECTOR vectorProduct leftOuterRotaryEncoderKnob.bottomVector
         ) {
            cylinder(
               LeftOuterRotaryEncoderKnob.HEIGHT,
               LeftOuterRotaryEncoderKnob.RADIUS,
               `$fa`
            )
         }
      }
      - internalGear(leftOuterRotaryEncoderKnob.internalGear)
   )
}
