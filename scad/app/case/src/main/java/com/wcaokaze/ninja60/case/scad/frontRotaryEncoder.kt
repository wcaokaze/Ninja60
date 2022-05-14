package com.wcaokaze.ninja60.case.scad

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.case.*
import com.wcaokaze.ninja60.parts.key.*
import com.wcaokaze.ninja60.parts.key.alphanumeric.*
import com.wcaokaze.ninja60.parts.rotaryencoder.*
import com.wcaokaze.ninja60.parts.rotaryencoder.front.*
import com.wcaokaze.ninja60.shared.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.ninja60.shared.scadutil.*
import com.wcaokaze.ninja60.shared.scadutil.Cube
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

internal fun PropagatedValueProvider.frontRotaryEncoderKnobHoleZOffset(): Size
      = RotaryEncoder.CLICK_TRAVEL + PrinterAdjustments.movableMargin.value

internal fun ScadParentObject.frontRotaryEncoderKnobCase(
   case: Case,
   radiusOffset: Size = 0.mm
): ScadObject {
   return intersection {
      place(case.frontRotaryEncoderKnob) {
         translate(z = (-200).mm) {
            cylinder(
               height = 400.mm,
               radius = FrontRotaryEncoderKnob.RADIUS
                     + PrinterAdjustments.movableMargin.value
                     + radiusOffset
            )
         }
      }

      hugeCube(
         topPlane = alphanumericTopPlaneLeft(case.alphanumericPlate, offset = 0.mm),
         bottomPlane = alphanumericBottomPlane(case, offset = 0.mm)
      )
   }
}

internal fun ScadParentObject.frontRotaryEncoderKnobHole(
   knob: FrontRotaryEncoderKnob,
   bottomOffset: Size = 0.mm,
   radiusOffset: Size = 0.mm
): ScadObject {
   return place(knob) {
      translate(z = -frontRotaryEncoderKnobHoleZOffset() - bottomOffset) {
         cylinder(
            FrontRotaryEncoderKnob.HEIGHT * 2,
            FrontRotaryEncoderKnob.RADIUS
                  + PrinterAdjustments.movableMargin.value
                  + radiusOffset
         )
      }
   }
}

internal fun ScadParentObject.frontRotaryEncoderHole(
   rotaryEncoder: RotaryEncoder,
   bottomOffset: Size = 0.mm,
   otherOffsets: Size = 0.mm
): ScadObject {
   return cube(Cube(
      rotaryEncoder.referencePoint
         .translate(rotaryEncoder.leftVector,  RotaryEncoder.BODY_SIZE.x / 2 + otherOffsets)
         .translate(rotaryEncoder.frontVector, RotaryEncoder.BODY_SIZE.y / 2 + otherOffsets)
         .translate(rotaryEncoder.bottomVector, bottomOffset),
      Size3d(
         RotaryEncoder.BODY_SIZE.x + otherOffsets * 2,
         RotaryEncoder.BODY_SIZE.y + otherOffsets * 2,
         RotaryEncoder.HEIGHT + bottomOffset
      ),
      rotaryEncoder.frontVector,
      rotaryEncoder.bottomVector
   ))
}

fun ScadParentObject.frontRotaryEncoderKeyCase(
   case: Case,
   height: Size,
   offset: Size = 0.mm
): ScadObject {
   return union {
      place(case.frontRotaryEncoderKey) {
         translate(z = -height) {
            val radius = FrontRotaryEncoderKey.RADIUS + FrontRotaryEncoderKey.KEY_WIDTH / 2

            val frontAngle = -Angle.PI / 2

            val startAngle = frontAngle - FrontRotaryEncoderKey.ARC_ANGLE / 2
            val endAngle   = frontAngle + FrontRotaryEncoderKey.ARC_ANGLE / 2

            arcCylinder(radius + offset, height, startAngle, endAngle, offset)
         }
      }
      marginFiller(case, surfaceOffsets = offset, internalOffsets = 0.1.mm)
   }
}

internal fun frontRotaryEncoderKeyCaseTopPlane(
   key: FrontRotaryEncoderKey,
   offset: Size
) = Plane3d(
   key.referencePoint
      .translate(key.topVector, KeySwitch.TRAVEL + offset),
   key.topVector
)

internal fun frontRotaryEncoderKeyCaseBottomPlane(
   key: FrontRotaryEncoderKey,
   offset: Size
) = Plane3d(
   key.referencePoint
      .translate(key.bottomVector, Case.FRONT_ROTARY_ENCODER_KEY_CASE_HEIGHT + offset),
   key.bottomVector
)

fun ScadParentObject.frontRotaryEncoderKeyHole(
   key: FrontRotaryEncoderKey,
   height: Size,
   bottomOffset: Size = 0.mm,
   innerRadiusOffset: Size = 0.mm,
   otherOffsets: Size = 0.mm
): ScadObject {
   return place(key) {
      translate(z = -bottomOffset) {
         val outerRadius = FrontRotaryEncoderKey.RADIUS + FrontRotaryEncoderKey.KEY_WIDTH / 2
         val innerRadius = FrontRotaryEncoderKey.RADIUS - FrontRotaryEncoderKey.KEY_WIDTH / 2

         val frontAngle = -Angle.PI / 2

         val startAngle = frontAngle - FrontRotaryEncoderKey.ARC_ANGLE / 2
         val endAngle   = frontAngle + FrontRotaryEncoderKey.ARC_ANGLE / 2

         arcCylinder(
            innerRadius - innerRadiusOffset,
            outerRadius + otherOffsets,
            height + bottomOffset,
            startAngle,
            endAngle,
            otherOffsets
         )
      }
   }
}

/**
 * [AlphanumericPlate]と[FrontRotaryEncoderKey]の隙間を埋めるやつ
 *
 * @param surfaceOffsets
 * [AlphanumericPlate]など他のパーツと接触せず、壁として露出する部分のオフセット
 * @param internalOffsets
 * [AlphanumericPlate]など他のパーツと接触している部分のオフセット
 */
private fun ScadParentObject.marginFiller(
   case: Case,
   surfaceOffsets: Size,
   internalOffsets: Size
): ScadObject {
   val frontRotaryEncoderKeyEndPoint = arcEndPoint(
      origin = case.frontRotaryEncoderKey.referencePoint,
      xAxis = case.frontRotaryEncoderKey.frontVector,
      yAxis = case.frontRotaryEncoderKey.rightVector,
      arcRadius = FrontRotaryEncoderKey.RADIUS
            + FrontRotaryEncoderKey.KEY_WIDTH / 2 + surfaceOffsets,
      angle = FrontRotaryEncoderKey.ARC_ANGLE / 2,
      surfaceOffsets
   )

   return distortedCube(
      leftPlane = Plane3d(case.frontRotaryEncoderKey.referencePoint, case.leftVector),
      rightPlane = Plane3d(
         frontRotaryEncoderKeyEndPoint,
         case.frontRotaryEncoderKey.frontVector.rotate(
            case.frontRotaryEncoderKey.topVector,
            FrontRotaryEncoderKey.ARC_ANGLE / 2
         )
      ),
      frontPlane = Plane3d(
         frontRotaryEncoderKeyEndPoint,
         case.frontRotaryEncoderKey.frontVector.rotate(
            case.frontRotaryEncoderKey.topVector,
            FrontRotaryEncoderKey.ARC_ANGLE / 2 + 90.deg
         )
      ),
      backPlane = alphanumericFrontPlaneRight(
         case.alphanumericPlate, case.thumbHomeKey, -internalOffsets),
      bottomPlane = frontRotaryEncoderKeyCaseBottomPlane(case.frontRotaryEncoderKey, surfaceOffsets),
      topPlane = alphanumericTopPlaneRight(case.alphanumericPlate, surfaceOffsets)
   )
}
