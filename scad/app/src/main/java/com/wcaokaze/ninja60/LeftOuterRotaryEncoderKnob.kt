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

      val INNER_KNOB_DEPTH = 1.5.mm

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
               -alphanumericPlate.leftmostPlane.normalVector
                     vectorProduct -Vector3d.Z_UNIT_VECTOR,
               -Vector3d.Z_UNIT_VECTOR,
               tangencyPoint
            )
            .let { it.translate(it.leftVector, RADIUS + 2.mm) }
            .let { it.translate(it.backVector, keyPitch.y / 3) }
            .let { it.translate(it.bottomVector, HEIGHT) }
      }
   }

   val internalGear get() = InternalGear(
      MODULE, INTERNAL_GEAR_TOOTH_COUNT, HEIGHT,
      referencePoint.translate(bottomVector, INNER_KNOB_DEPTH + THICKNESS),
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
            difference {
               cylinder(
                  LeftOuterRotaryEncoderKnob.HEIGHT,
                  LeftOuterRotaryEncoderKnob.RADIUS,
                  `$fa`
               )

               translate(
                  z = LeftOuterRotaryEncoderKnob.HEIGHT
                        - LeftOuterRotaryEncoderKnob.INNER_KNOB_DEPTH
               ) {
                  cylinder(
                     LeftOuterRotaryEncoderKnob.INNER_KNOB_DEPTH * 2,
                     LeftInnerRotaryEncoderKnob.RADIUS + 0.7.mm,
                     `$fa`
                  )
               }

               cylinder(
                  LeftOuterRotaryEncoderKnob.HEIGHT,
                  RotaryEncoder.SHAFT_RADIUS + 0.5.mm,
                  `$fa`
               )
            }
         }
      }
      - internalGear(leftOuterRotaryEncoderKnob.internalGear)
   )
}
