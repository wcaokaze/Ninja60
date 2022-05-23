package com.wcaokaze.ninja60.case

import com.wcaokaze.scadwriter.linearalgebra.*
import com.wcaokaze.ninja60.case.scad.*
import com.wcaokaze.ninja60.parts.key.alphanumeric.*
import com.wcaokaze.ninja60.parts.rotaryencoder.back.*
import com.wcaokaze.ninja60.shared.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

fun PropagatedValueProvider.BackRotaryEncoderGearHolderRightArm(
   alphanumericPlate: AlphanumericPlate,
   backRotaryEncoderKnob: BackRotaryEncoderKnob
) = BackRotaryEncoderGearHolderRightArm(
   this,
   alphanumericPlate,
   backRotaryEncoderKnob
)

data class BackRotaryEncoderGearHolderRightArm(
   override val referencePoint: Point3d,
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   val armLength: Size,
   val protuberanceSize: Size
) : TransformableDefaultImpl<BackRotaryEncoderGearHolderRightArm> {
   companion object {
      operator fun invoke(
         propagatedValueProvider: PropagatedValueProvider,
         alphanumericPlate: AlphanumericPlate,
         backRotaryEncoderKnob: BackRotaryEncoderKnob
      ): BackRotaryEncoderGearHolderRightArm {
         val knobCenterPoint = backRotaryEncoderKnob.referencePoint
            .translate(backRotaryEncoderKnob.topVector, BackRotaryEncoderKnob.HEIGHT)

         val armPlane = Plane3d(knobCenterPoint, backRotaryEncoderKnob.topVector)

         val alphanumericPlane = alphanumericBackSlopePlane(
            alphanumericPlate,
            offset = 0.mm
         )

         val armRootLine = armPlane intersection alphanumericPlane

         val startPoint = Plane3d(knobCenterPoint, armRootLine.vector) intersection armRootLine

         val holderRootRadius = with (propagatedValueProvider) {
            BackRotaryEncoderKnob.RADIUS +
                  BackRotaryEncoderKnob.SKIDPROOF_RADIUS +
                  PrinterAdjustments.movableMargin.value
         }

         val armRootPoint = (
               startPoint
                  ..startPoint.translate(-armRootLine.vector, holderRootRadius * 1.5)
                  step 0.05.mm
            )
            .first { it distance knobCenterPoint > holderRootRadius }

         val protuberanceSize = with (propagatedValueProvider) {
            PrinterAdjustments.minProtuberanceSize.value
         }

         return BackRotaryEncoderGearHolderRightArm(
            armRootPoint
               .translate(backRotaryEncoderKnob.topVector,
                  with (propagatedValueProvider) {
                     protuberanceSize.z + PrinterAdjustments.movableMargin.value / 2
                  }
               ),
            frontVector = -backRotaryEncoderKnob.topVector
                  vectorProduct Vector3d(armRootPoint, knobCenterPoint),
            bottomVector = -backRotaryEncoderKnob.topVector,
            armLength = armRootPoint distance knobCenterPoint,
            protuberanceSize.x
         )
      }
   }

   override fun copy(
      referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d
   ) = BackRotaryEncoderGearHolderRightArm(
      referencePoint, frontVector, bottomVector, armLength, protuberanceSize
   )
}
