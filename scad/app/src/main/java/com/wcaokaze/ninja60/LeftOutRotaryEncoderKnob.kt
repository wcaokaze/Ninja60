package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

class LeftOutRotaryEncoderKnob(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : Transformable<LeftOutRotaryEncoderKnob> {
   companion object {
      /** 奥から何番目の[KeySwitch]にノブを配置するか */
      val ROW_INDEX = 2

      val MODULE = 1.mm
      val RADIUS = 30.mm
      val HEIGHT = 14.mm

      operator fun invoke(alphanumericPlate: AlphanumericPlate): LeftOutRotaryEncoderKnob {
         val leftmostColumn = alphanumericPlate.columns.first()
         val keySwitch = leftmostColumn.keySwitches[ROW_INDEX]

         val keycapTop = keySwitch.referencePoint
            .translate(keySwitch.topVector,
               KeySwitch.TOP_HEIGHT + KeySwitch.STEM_HEIGHT + Keycap.THICKNESS)

         val tangencyPoint = alphanumericPlate.leftmostPlane intersection
               Line3d(keycapTop, keySwitch.leftVector)

         return LeftOutRotaryEncoderKnob(
            Vector3d.Y_UNIT_VECTOR,
            -Vector3d.Z_UNIT_VECTOR,
            tangencyPoint
               .translate(-Vector3d.X_UNIT_VECTOR, RADIUS + 2.mm)
               .translate( Vector3d.Y_UNIT_VECTOR, keyPitch.y / 3)
               .translate(-Vector3d.Z_UNIT_VECTOR, HEIGHT)
         )
      }
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
      =  LeftOutRotaryEncoderKnob(frontVector, bottomVector, referencePoint)
}

fun ScadParentObject.leftOutRotaryEncoderKnob(
   leftOutRotaryEncoderKnob: LeftOutRotaryEncoderKnob
): ScadObject {
   return locale(leftOutRotaryEncoderKnob.referencePoint) {
      rotate(
         -Vector3d.Z_UNIT_VECTOR angleWith leftOutRotaryEncoderKnob.bottomVector,
         -Vector3d.Z_UNIT_VECTOR vectorProduct leftOutRotaryEncoderKnob.bottomVector
      ) {
         cylinder(
            LeftOutRotaryEncoderKnob.HEIGHT,
            LeftOutRotaryEncoderKnob.RADIUS,
            `$fa`
         )
      }
   }
}
