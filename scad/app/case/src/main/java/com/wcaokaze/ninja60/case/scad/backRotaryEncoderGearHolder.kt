package com.wcaokaze.ninja60.case.scad

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.case.*
import com.wcaokaze.ninja60.parts.key.*
import com.wcaokaze.ninja60.parts.key.alphanumeric.*
import com.wcaokaze.ninja60.parts.rotaryencoder.back.*
import com.wcaokaze.ninja60.shared.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

internal fun ScadParentObject.backRotaryEncoderGearHolder(
   case: Case
): ScadObject {
   return union {
      backRotaryEncoderGearHolderLeftArm(case.backRotaryEncoderGearHolderLeftArm)
      backRotaryEncoderGearHolderLeftArmSupportWall(case)
      backRotaryEncoderGearHolderRightArm(
         case.backRotaryEncoderGearHolderRightArm,
         case.backRotaryEncoderKnob,
         case.alphanumericPlate
      )
   }
}

internal fun armPlane(
   leftArm: BackRotaryEncoderGearHolderLeftArm,
   offset: Size
): Plane3d {
   return Plane3d(
         leftArm.referencePoint,
         leftArm.topVector
      )
      .translateNormalVector(offset)
}

internal fun armPlane(
   rightArm: BackRotaryEncoderGearHolderRightArm,
   offset: Size
): Plane3d {
   return Plane3d(
         rightArm.referencePoint,
         rightArm.topVector
      )
      .translateNormalVector(offset)
}

internal fun ScadParentObject.backRotaryEncoderGearHolderLeftArm(
   backRotaryEncoderGearHolderLeftArm: BackRotaryEncoderGearHolderLeftArm
): ScadObject {
   return place(backRotaryEncoderGearHolderLeftArm) {
      minkowski {
         cube(
            backRotaryEncoderGearHolderLeftArm.armLength,
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
            intersection armPlane(case.backRotaryEncoderGearHolderLeftArm, offset = 0.mm),
         case.frontVector
      )
   }

   val topPlane = run {
      val v = Vector3d(case.backRotaryEncoderMediationGear.referencePoint,
                       case.backRotaryEncoderKnob.gearReferencePoint)
      Plane3d(
         case.backRotaryEncoderKnob.gearReferencePoint,
         v vectorProduct armPlane(case.backRotaryEncoderGearHolderLeftArm, offset = 0.mm).normalVector
      )
   }

   return distortedCube(
      leftPlane = armPlane(case.backRotaryEncoderGearHolderLeftArm,
         offset = PrinterAdjustments.minWallThickness.value),
      rightPlane = armPlane(case.backRotaryEncoderGearHolderLeftArm,
         offset = 0.mm),
      frontPlane = frontPlane,
      backPlane = backRotaryEncoderCaseGearSideFrontPlane(
         case.backRotaryEncoderGear.rotaryEncoder, offset = (-0.1).mm),
      bottomPlane = alphanumericBackSlopePlane(case.alphanumericPlate, offset = 0.mm),
      topPlane = topPlane
   )
}

internal fun ScadParentObject.backRotaryEncoderGearHolderRightArm(
   backRotaryEncoderGearHolderRightArm: BackRotaryEncoderGearHolderRightArm,
   backRotaryEncoderKnob: BackRotaryEncoderKnob,
   alphanumericPlate: AlphanumericPlate
): ScadObject {
   val knobHollowRadius = BackRotaryEncoderKnob.RADIUS +
         BackRotaryEncoderKnob.SKIDPROOF_RADIUS +
         PrinterAdjustments.movableMargin.value

   var scad: ScadObject = intersection {
      place(backRotaryEncoderGearHolderRightArm) {
         minkowski {
            cube(backRotaryEncoderGearHolderRightArm.armLength, 0.01.mm, 0.01.mm)

            translate(z = -Case.BACK_ROTARY_ENCODER_GEAR_HOLDER_ARM_WIDTH) {
               cylinder(
                  height = Case.BACK_ROTARY_ENCODER_GEAR_HOLDER_ARM_WIDTH
                        + PrinterAdjustments.minWallThickness.value,
                  radius = Case.BACK_ROTARY_ENCODER_GEAR_HOLDER_ARM_WIDTH / 2)
            }
         }
      }

      place(backRotaryEncoderKnob) {
         cylinder(
            height = BackRotaryEncoderKnob.HEIGHT
                  + PrinterAdjustments.minWallThickness.value,
            radius = knobHollowRadius
                  + PrinterAdjustments.minWallThickness.value)
      }
   }

   // ---- rightArmとalphanumericBackSlopeをつなぐ壁
   val encoderColumn = alphanumericPlate
      .columns[Case.BACK_ROTARY_ENCODER_COLUMN_INDEX]
   val encoderKey = encoderColumn.keySwitches.first()
   val encoderKeyBottomPlane = Plane3d(encoderKey.referencePoint, encoderKey.bottomVector)
      .translateNormalVector(KeySwitch.BOTTOM_HEIGHT)

   scad += distortedCube(
      leftPlane = armPlane(backRotaryEncoderGearHolderRightArm,
         offset = -Case.BACK_ROTARY_ENCODER_GEAR_HOLDER_ARM_WIDTH),
      rightPlane = armPlane(backRotaryEncoderGearHolderRightArm,
         offset = PrinterAdjustments.minWallThickness.value),
      frontPlane = Plane3d(
         backRotaryEncoderGearHolderRightArm.referencePoint,
         backRotaryEncoderGearHolderRightArm.frontVector),
      backPlane = encoderKeyBottomPlane,
      bottomPlane = alphanumericBackSlopePlane(alphanumericPlate,
         offset = -PrinterAdjustments.minWallThickness.value / 4),
      topPlane = alphanumericBackSlopePlane(alphanumericPlate,
         offset = PrinterAdjustments.minWallThickness.value)
   )

   // ---- ↑の壁と右隣のキーとの間にできる隙間埋め
   val rightColumn = alphanumericPlate
      .columns.getOrNull(Case.BACK_ROTARY_ENCODER_COLUMN_INDEX + 1)

   if (rightColumn != null) {
      val rightKey = rightColumn.keySwitches.first()
      val encoderColumnPlane = Plane3d(encoderKey.referencePoint, rightKey.topVector)
         .translateNormalVector(KeySwitch.TRAVEL)
      val rightColumnPlane = Plane3d(rightKey.referencePoint, rightKey.topVector)
         .translateNormalVector(KeySwitch.TRAVEL)

      if (encoderColumnPlane < rightColumnPlane) {
         scad += distortedCube(
            leftPlane = armPlane(backRotaryEncoderGearHolderRightArm, offset = (-0.1).mm),
            rightPlane = getWallPlane(encoderColumn, rightColumn),
            frontPlane = rightColumnPlane,
            backPlane = encoderKeyBottomPlane,
            bottomPlane = Plane3d(
               rightKey.plate(AlphanumericPlate.KEY_PLATE_SIZE).backLeft,
               rightKey.frontVector
            ),
            topPlane = alphanumericBackSlopePlane(alphanumericPlate,
               offset = PrinterAdjustments.minWallThickness.value)
         )
      }
   }

   // ---- ノブが入る部分ごっそり削る
   scad -= place(backRotaryEncoderKnob) {
      cylinder(
         BackRotaryEncoderKnob.HEIGHT,
         knobHollowRadius)
   }

   return scad
}
