package com.wcaokaze.ninja60.case.scad

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.case.*
import com.wcaokaze.ninja60.parts.key.alphanumeric.*
import com.wcaokaze.ninja60.parts.rotaryencoder.*
import com.wcaokaze.ninja60.parts.rotaryencoder.gear.*
import com.wcaokaze.ninja60.shared.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

internal fun ScadParentObject.backRotaryEncoderGearSideCase(case: Case): ScadObject {
   val wallThickness = PrinterAdjustments.minWallThickness.value

   return distortedCube(
      leftPlane = backRotaryEncoderCaseLeftPlane(case, wallThickness),
      rightPlane = backRotaryEncoderCaseRightPlane(
         case.backRotaryEncoderGear.rotaryEncoder, wallThickness),
      frontPlane = backRotaryEncoderCaseGearSideFrontPlane(
         case.backRotaryEncoderGear.rotaryEncoder, offset = 0.mm),
      backPlane = Plane3d(
            case.backRotaryEncoderGear.rotaryEncoder.referencePoint,
            case.backRotaryEncoderGear.rotaryEncoder.bottomVector
         ).translateNormalVector(RotaryEncoder.BOARD_THICKNESS),
      bottomPlane = backRotaryEncoderCaseGearSideBottomPlane(
         case.alphanumericPlate, offset = 0.mm),
      topPlane = backRotaryEncoderCaseTopPlane(
         case.alphanumericPlate, case.backRotaryEncoderGear.rotaryEncoder, wallThickness)
   )
}

internal fun ScadParentObject.backRotaryEncoderGearSideHollow(case: Case): ScadObject {
   val wallThickness = PrinterAdjustments.minWallThickness.value

   return distortedCube(
      leftPlane = backRotaryEncoderCaseLeftPlane(case, offset = 0.mm),
      rightPlane = backRotaryEncoderCaseRightPlane(
         case.backRotaryEncoderGear.rotaryEncoder, offset = 0.mm),
      frontPlane = backRotaryEncoderCaseGearSideFrontPlane(
         case.backRotaryEncoderGear.rotaryEncoder, offset = 0.1.mm),
      backPlane = Plane3d(
         case.backRotaryEncoderGear.rotaryEncoder.referencePoint,
         case.backRotaryEncoderGear.rotaryEncoder.bottomVector),
      bottomPlane = backRotaryEncoderCaseGearSideBottomPlane(
         case.alphanumericPlate, offset = -wallThickness),
      topPlane = backRotaryEncoderCaseTopPlane(
         case.alphanumericPlate, case.backRotaryEncoderGear.rotaryEncoder, offset = 0.mm)
   )
}

internal fun ScadParentObject.backRotaryEncoderCircuitSideCase(case: Case): ScadObject {
   val wallThickness = PrinterAdjustments.minWallThickness.value

   return distortedCube(
      leftPlane = backRotaryEncoderCaseLeftPlane(case, wallThickness),
      rightPlane = backRotaryEncoderCaseRightPlane(
         case.backRotaryEncoderGear.rotaryEncoder, wallThickness),
      frontPlane = backRotaryEncoderCaseGearSideFrontPlane(
         case.backRotaryEncoderGear.rotaryEncoder, offset = 0.mm),
      backPlane = backRotaryEncoderCaseCircuitSideBackPlane(
         case.backRotaryEncoderGear.rotaryEncoder, wallThickness),
      bottomPlane = backRotaryEncoderCaseCircuitSideBottomPlane(case, offset = 0.mm),
      topPlane = backRotaryEncoderCaseTopPlane(
         case.alphanumericPlate, case.backRotaryEncoderGear.rotaryEncoder, wallThickness)
   )
}

internal fun ScadParentObject.backRotaryEncoderCircuitSideCave(case: Case): ScadObject {
   return distortedCube(
      leftPlane = backRotaryEncoderCaseLeftPlane(case, offset = 0.mm),
      rightPlane = backRotaryEncoderCaseRightPlane(
         case.backRotaryEncoderGear.rotaryEncoder, offset = 0.mm),
      frontPlane = backRotaryEncoderCaseGearSideFrontPlane(
         case.backRotaryEncoderGear.rotaryEncoder, offset = 0.mm),
      backPlane = backRotaryEncoderCaseCircuitSideBackPlane(
         case.backRotaryEncoderGear.rotaryEncoder, offset = 0.mm),
      bottomPlane = backRotaryEncoderCaseCircuitSideBottomPlane(case, offset = 0.1.mm),
      topPlane = backRotaryEncoderCaseTopPlane(
         case.alphanumericPlate, case.backRotaryEncoderGear.rotaryEncoder, offset = 0.mm)
   )
}

internal fun backRotaryEncoderCaseLeftPlane(case: Case, offset: Size): Plane3d
      = backRotaryEncoderGearPlane(case, offset)

internal fun backRotaryEncoderCaseRightPlane(
   backRotaryEncoder: RotaryEncoder,
   offset: Size
): Plane3d {
   return Plane3d(
         backRotaryEncoder.referencePoint.translate(
            backRotaryEncoder.frontVector,
            RotaryEncoder.BODY_SIZE.y / 2 + Case.BACK_ROTARY_ENCODER_CASE_MARGIN_SPACE
         ),
         backRotaryEncoder.frontVector
      )
      .translateNormalVector(offset)
}

internal fun backRotaryEncoderCaseGearSideFrontPlane(
   backRotaryEncoder: RotaryEncoder,
   offset: Size
): Plane3d {
   return Plane3d(
         backRotaryEncoder.referencePoint
            .translate(backRotaryEncoder.topVector, RotaryEncoder.BODY_SIZE.z),
         backRotaryEncoder.topVector
      )
      .translateNormalVector(offset)
}

internal fun backRotaryEncoderCaseCircuitSideBackPlane(
   backRotaryEncoder: RotaryEncoder,
   offset: Size
): Plane3d {
   return Plane3d(
         backRotaryEncoder.referencePoint.translate(
            backRotaryEncoder.bottomVector,
            RotaryEncoder.BOARD_THICKNESS + Case.BACK_ROTARY_ENCODER_CASE_DEPTH
         ),
         backRotaryEncoder.bottomVector
      )
      .translateNormalVector(offset)
}

internal fun backRotaryEncoderCaseGearSideBottomPlane(
   alphanumericPlate: AlphanumericPlate,
   offset: Size
): Plane3d = alphanumericBackSlopePlane(alphanumericPlate, -offset)

internal fun backRotaryEncoderCaseCircuitSideBottomPlane(
   case: Case,
   offset: Size
): Plane3d = alphanumericBottomPlane(case, offset)

internal fun backRotaryEncoderCaseTopPlane(
   alphanumericPlate: AlphanumericPlate,
   backRotaryEncoder: RotaryEncoder,
   offset: Size
): Plane3d {
   val p = backRotaryEncoderCaseGearSideBottomPlane(alphanumericPlate, offset = 0.mm)

   return sequenceOf(
         backRotaryEncoder.referencePoint
            .translate(backRotaryEncoder.rightVector, RotaryEncoder.BODY_SIZE.x / 2)
            .translate(backRotaryEncoder.backVector,  RotaryEncoder.BODY_SIZE.y / 2),
         backRotaryEncoder.referencePoint
            .translate(backRotaryEncoder.rightVector, RotaryEncoder.BODY_SIZE.x / 2)
            .translate(backRotaryEncoder.frontVector, RotaryEncoder.BODY_SIZE.y / 2)
      )
      .map { Plane3d(it, p.normalVector) }
      .maxWithOrNull(Plane3d::compareTo)!!
      .translateNormalVector(offset)
}

internal fun backRotaryEncoderGearPlane(case: Case, offset: Size): Plane3d {
   fun Gear.leftVector() = if (bottomVector isSameDirection case.leftVector) {
      bottomVector
   } else {
      topVector
   }

   fun Gear.leftPoint() = if (bottomVector isSameDirection case.leftVector) {
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
            .map { Vector3d(g.leftPoint(), it) }
            .all { it isSameDirection g.leftVector() }
      }
   }

   val leftmostGear = leftmost(
      case.backRotaryEncoderKnob.gear,
      case.backRotaryEncoderMediationGear.spurGear
   )

   return leftmostGear.plane().translateNormalVector(offset)
}
