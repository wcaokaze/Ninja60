package com.wcaokaze.ninja60.case.scad

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.case.*
import com.wcaokaze.ninja60.shared.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

internal fun ScadParentObject.backRotaryEncoderGearHolder(
   case: Case
): ScadObject {
   return union {
      backRotaryEncoderGearHolderLeftArm(case)
      backRotaryEncoderGearHolderLeftArmSupportWall(case)
   }
}

internal fun ScadParentObject.backRotaryEncoderGearHolderLeftArm(
   case: Case
): ScadObject {
   data class LeftArm(
      override val frontVector: Vector3d,
      override val bottomVector: Vector3d,
      override val referencePoint: Point3d
   ) : TransformableDefaultImpl<LeftArm> {
      override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
            = LeftArm(frontVector, bottomVector, referencePoint)
   }

   val armRootPoint = backRotaryEncoderCaseGearSideFrontPlane(
      case.backRotaryEncoderGear.rotaryEncoder, offset = (-0.1).mm
   ) intersection Line3d(
      case.backRotaryEncoderMediationGear.referencePoint,
      case.backRotaryEncoderKnob.gearReferencePoint
   )

   val topVector = backRotaryEncoderCaseLeftPlane(case, offset = 0.mm).normalVector

   return place(LeftArm(
      frontVector = Vector3d(
         case.backRotaryEncoderMediationGear.referencePoint,
         case.backRotaryEncoderKnob.gearReferencePoint
      ) vectorProduct topVector,
      bottomVector = -topVector,
      armRootPoint
   )) {
      minkowski {
         cube(
            armRootPoint distance case.backRotaryEncoderKnob.gearReferencePoint,
            0.01.mm, 0.01.mm
         )

         cylinder(PrinterAdjustments.minWallThickness.value,
            Case.BACK_ROTARY_ENCODER_GEAR_HOLDER_ARM_WIDTH / 2)
      }
   }
}

internal fun ScadParentObject.backRotaryEncoderGearHolderLeftArmSupportWall(
   case: Case
): ScadObject {
   val frontPlane = run {
      val key = case.alphanumericPlate
         .columns[Case.BACK_ROTARY_ENCODER_COLUMN_INDEX]
         .keySwitches.first()

      val keyPlane = Plane3d(key.referencePoint, key.topVector)

      Plane3d(
         keyPlane
            intersection alphanumericBackSlopePlane(case.alphanumericPlate, offset = 0.mm)
            intersection backRotaryEncoderGearPlane(case, offset = 0.mm),
         case.frontVector
      )
   }

   val topPlane = run {
      val v = Vector3d(case.backRotaryEncoderMediationGear.referencePoint,
                       case.backRotaryEncoderKnob.gearReferencePoint)
      Plane3d(
         case.backRotaryEncoderKnob.gearReferencePoint,
         v vectorProduct backRotaryEncoderGearPlane(case, offset = 0.mm).normalVector
      )
   }

   return distortedCube(
      leftPlane = backRotaryEncoderGearPlane(
         case, offset = PrinterAdjustments.minWallThickness.value),
      rightPlane = backRotaryEncoderGearPlane(case, offset = 0.mm),
      frontPlane = frontPlane,
      backPlane = backRotaryEncoderCaseGearSideFrontPlane(
         case.backRotaryEncoderGear.rotaryEncoder, offset = (-0.1).mm),
      bottomPlane = alphanumericBackSlopePlane(case.alphanumericPlate, offset = 0.mm),
      topPlane = topPlane
   )
}
