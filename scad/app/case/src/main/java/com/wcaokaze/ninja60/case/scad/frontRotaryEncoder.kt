package com.wcaokaze.ninja60.case.scad

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.case.*
import com.wcaokaze.ninja60.parts.rotaryencoder.*
import com.wcaokaze.ninja60.parts.rotaryencoder.front.*
import com.wcaokaze.ninja60.shared.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.ninja60.shared.scadutil.*
import com.wcaokaze.ninja60.shared.scadutil.Cube
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

internal fun ScadObject.frontRotaryEncoderKnobHoleZOffset(): Size
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
   key: FrontRotaryEncoderKey,
   height: Size,
   offset: Size = 0.mm
): ScadObject {
   return place(key) {
      translate(z = -height) {
         difference {
            val radius = FrontRotaryEncoderKey.RADIUS + FrontRotaryEncoderKey.KEY_WIDTH / 2

            val frontAngle = -Angle.PI / 2

            val startAngle = frontAngle - FrontRotaryEncoderKey.ARC_ANGLE / 2
            val endAngle   = frontAngle + FrontRotaryEncoderKey.ARC_ANGLE / 2

            arcCylinder(radius + offset, height, startAngle, endAngle, offset)
         }
      }
   }
}

fun ScadParentObject.frontRotaryEncoderKeyHole(
   key: FrontRotaryEncoderKey,
   height: Size,
   bottomOffset: Size = 0.mm,
   innerRadiusOffset: Size = 0.mm,
   otherOffsets: Size = 0.mm
): ScadObject {
   return place(key) {
      translate(z = -bottomOffset) {
         difference {
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
}
