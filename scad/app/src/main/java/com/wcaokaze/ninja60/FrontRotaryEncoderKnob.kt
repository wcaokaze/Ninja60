package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

class FrontRotaryEncoderKnob(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : Transformable<FrontRotaryEncoderKnob> {
   companion object {
      /** 何番目の[AlphanumericColumn]にノブを配置するか */
      val COLUMN_INDEX = 3

      val RADIUS = 18.mm
      val HEIGHT = 14.mm
      val HOLE_HEIGHT = HEIGHT - 2.mm

      operator fun invoke(alphanumericPlate: AlphanumericPlate): FrontRotaryEncoderKnob {
         val caseTopPlane = alphanumericTopPlane(alphanumericPlate, 0.mm)

         val column = alphanumericPlate.columns[COLUMN_INDEX]
         val columnPlane = Plane3d(column.referencePoint, column.rightVector)

         val mostFrontKey = column.keySwitches.last()
         val mostFrontKeycapTopPlane = Plane3d(mostFrontKey.referencePoint, mostFrontKey.topVector)
            .translate(mostFrontKey.topVector, KeySwitch.TOP_HEIGHT + KeySwitch.STEM_HEIGHT + Keycap.THICKNESS)

         val knobCenter = (
               caseTopPlane
                  .translate(-caseTopPlane.normalVector, 2.mm)
            ) intersection (
               columnPlane
            ) intersection (
               mostFrontKeycapTopPlane
                  .translate(mostFrontKey.bottomVector, RADIUS)
                  .translate(mostFrontKey.bottomVector, KeySwitch.TRAVEL)
                  .translate(mostFrontKey.bottomVector, 2.mm)
            )

         return FrontRotaryEncoderKnob(
            column.frontVector vectorProduct caseTopPlane.normalVector,
            -caseTopPlane.normalVector,
            knobCenter
         )
      }
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
         cylinder(FrontRotaryEncoderKnob.HEIGHT, FrontRotaryEncoderKnob.RADIUS)
      }
      - rotaryEncoderKnobHole(knob.rotaryEncoder)
   )
}