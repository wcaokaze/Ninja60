package com.wcaokaze.ninja60.case

import com.wcaokaze.scadwriter.linearalgebra.*
import com.wcaokaze.ninja60.parts.rotaryencoder.*
import com.wcaokaze.ninja60.parts.rotaryencoder.back.*
import com.wcaokaze.ninja60.parts.rotaryencoder.gear.*
import com.wcaokaze.ninja60.shared.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

fun PropagatedValueProvider.BackRotaryEncoderGearHolderLeftArm(
   caseLeftVector: Vector3d,
   backRotaryEncoderKnob: BackRotaryEncoderKnob,
   backRotaryEncoderMediationGear: BackRotaryEncoderMediationGear,
   backRotaryEncoderGear: BackRotaryEncoderGear
) = BackRotaryEncoderGearHolderLeftArm(
   this, caseLeftVector, backRotaryEncoderKnob, backRotaryEncoderMediationGear,
   backRotaryEncoderGear
)

data class BackRotaryEncoderGearHolderLeftArm(
   override val referencePoint: Point3d,
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   val armLength: Size,
   val protuberanceSize: Size
) : TransformableDefaultImpl<BackRotaryEncoderGearHolderLeftArm> {
   companion object {
      val GEAR_SHAFT_HOLE_DEPTH = 3.mm

      operator fun invoke(
         propagatedValueProvider: PropagatedValueProvider,
         caseLeftVector: Vector3d,
         backRotaryEncoderKnob: BackRotaryEncoderKnob,
         backRotaryEncoderMediationGear: BackRotaryEncoderMediationGear,
         backRotaryEncoderGear: BackRotaryEncoderGear
      ): BackRotaryEncoderGearHolderLeftArm {
         val rotaryEncoder = backRotaryEncoderGear.rotaryEncoder

         val rotaryEncoderPlane = Plane3d(
            rotaryEncoder.referencePoint
               .translate(rotaryEncoder.topVector, RotaryEncoder.BODY_SIZE.z),
            rotaryEncoder.topVector
         )

         val gearPlane = gearPlane(
            caseLeftVector, backRotaryEncoderKnob, backRotaryEncoderMediationGear
         )

         val gearPerpendicularityPlane = Plane3d(
            backRotaryEncoderMediationGear.referencePoint,
            gearPlane.normalVector vectorProduct Vector3d(
               backRotaryEncoderMediationGear.referencePoint,
               backRotaryEncoderKnob.gearReferencePoint
            )
         )

         val armRootPoint = rotaryEncoderPlane intersection
               gearPlane intersection gearPerpendicularityPlane

         val protuberanceSize = with (propagatedValueProvider) {
            PrinterAdjustments.minProtuberanceSize.value
         }

         return BackRotaryEncoderGearHolderLeftArm(
            referencePoint = armRootPoint
               .translate(gearPlane.normalVector,
                  with (propagatedValueProvider) {
                     protuberanceSize.z + PrinterAdjustments.movableMargin.value / 2
                  }
               ),
            frontVector = Vector3d(
               backRotaryEncoderMediationGear.referencePoint,
               backRotaryEncoderKnob.gearReferencePoint
            ) vectorProduct gearPlane.normalVector,
            bottomVector = -gearPlane.normalVector,
            armLength = armRootPoint
                  distance backRotaryEncoderKnob.gearReferencePoint,
            protuberanceSize.x
         )
      }

      private fun gearPlane(
         caseLeftVector: Vector3d,
         backRotaryEncoderKnob: BackRotaryEncoderKnob,
         backRotaryEncoderMediationGear: BackRotaryEncoderMediationGear
      ): Plane3d {
         fun Gear.leftVector() = if (bottomVector isSameDirection caseLeftVector) {
            bottomVector
         } else {
            topVector
         }

         fun Gear.leftPoint() = if (bottomVector isSameDirection caseLeftVector) {
            referencePoint
         } else {
            referencePoint.translate(topVector, thickness)
         }

         fun Gear.leftVectorLine() = Line3d(leftPoint(), leftVector())
         fun Gear.plane() = Plane3d(leftPoint(), leftVector())

         fun leftmost(vararg gears: Gear): Gear {
            return gears.first { g ->
               gears.asSequence()
                  .minusElement(g)
                  .map { it.plane() intersection g.leftVectorLine() }
                  .all {
                     val v = Vector3d(g.leftPoint(), it)
                     v.norm < 0.1.mm || v isSameDirection g.leftVector()
                  }
            }
         }

         val leftmostGear = leftmost(
            backRotaryEncoderKnob.gear,
            backRotaryEncoderMediationGear.spurGear
         )

         return leftmostGear.plane()
      }
   }

   override fun copy(
      referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d
   ) = BackRotaryEncoderGearHolderLeftArm(
      referencePoint, frontVector, bottomVector, armLength, protuberanceSize
   )
}
