package com.wcaokaze.ninja60.case.scad

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.case.*
import com.wcaokaze.ninja60.parts.key.*
import com.wcaokaze.ninja60.parts.key.thumb.*
import com.wcaokaze.ninja60.shared.PrinterAdjustments
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.ninja60.shared.scadutil.*
import com.wcaokaze.ninja60.shared.scadutil.Cube
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

internal fun ScadParentObject.thumbKeyCase(
   thumbHomeKey: KeySwitch,
   thumbPlate: ThumbPlate,
   alphanumericFrontPlane: Plane3d,
   alphanumericBottomPlane: Plane3d,
   frontRotaryEncoderKeyBottomPlane: Plane3d,
   offsets: Size = 0.mm
): ScadObject {
   return intersection {
      hugeCube(
         leftPlane = thumbKeyCaseLeftPlane(thumbHomeKey, offsets),
         backPlane = thumbKeyCaseBackPlane(alphanumericFrontPlane, offsets),
         bottomPlane = thumbKeyCaseBottomPlane(alphanumericBottomPlane, offset = 0.mm),
         topPlane = thumbKeyCaseTopPlane(frontRotaryEncoderKeyBottomPlane, offsets)
      )

      union {
         distortedCube(
            thumbKeyCaseTopPlane(frontRotaryEncoderKeyBottomPlane, offsets),
            thumbKeyCaseLeftPlane(thumbHomeKey, offsets),
            thumbKeyCaseBackPlane(alphanumericFrontPlane, offsets),
            thumbKeyCaseRightPlane(thumbHomeKey, offsets),
            thumbKeyCaseFrontPlane(thumbHomeKey, offsets),
            thumbKeyCaseBottomPlane(alphanumericBottomPlane, offset = 0.mm)
         )

         thumbPlateHole(
            thumbPlate,
            height = KeySwitch.TRAVEL,
            bottomOffset = 20.mm + offsets,
            otherOffsets = offsets
         )
      }
   }
}

internal fun ScadObject.thumbKeyCaseLeftPlane(
   thumbHomeKey: KeySwitch,
   offset: Size
): Plane3d {
   return Plane3d(
         thumbHomeKey.referencePoint
            .translate(
               thumbHomeKey.bottomVector,
               Case.THUMB_HOME_KEY_CASE_HEIGHT + PrinterAdjustments.minWallThickness.value
            ),
         thumbHomeKey.bottomVector
      )
      .translateNormalVector(offset)
}

internal fun thumbKeyCaseRightPlane(
   thumbHomeKey: KeySwitch,
   offset: Size
): Plane3d {
   return Plane3d(
         thumbHomeKey.referencePoint
            .translate(thumbHomeKey.topVector, KeySwitch.TRAVEL),
         thumbHomeKey.topVector
      )
      .translateNormalVector(offset)
}

internal fun ScadObject.thumbKeyCaseFrontPlane(
   thumbHomeKey: KeySwitch,
   offset: Size
): Plane3d {
   return Plane3d(
         thumbHomeKey.referencePoint
            .translate(
               thumbHomeKey.frontVector,
               ThumbPlate.KEY_PLATE_SIZE.y / 2 + PrinterAdjustments.minWallThickness.value
            ),
         thumbHomeKey.frontVector
      )
      .translateNormalVector(offset)
}

internal fun thumbKeyCaseBackPlane(
   alphanumericFrontPlane: Plane3d,
   offset: Size
) = alphanumericFrontPlane.translateNormalVector(offset)

internal fun thumbKeyCaseBottomPlane(
   alphanumericBottomPlane: Plane3d,
   offset: Size
) = alphanumericBottomPlane.translateNormalVector(offset)

internal fun thumbKeyCaseTopPlane(
   frontRotaryEncoderKeyBottomPlane: Plane3d,
   offset: Size
) = frontRotaryEncoderKeyBottomPlane.translateNormalVector(offset)

internal fun ScadParentObject.thumbHomeKeyHole(
   key: KeySwitch,
   height: Size,
   bottomOffset: Size = 0.mm,
   frontOffset: Size = 0.mm,
   backOffset: Size = 0.mm,
   leftOffset: Size = 0.mm,
   rightOffset: Size = 0.mm
): ScadObject {
   val keyPlateSize = key.plate(ThumbPlate.KEY_PLATE_SIZE).size

   return cube(Cube(
      key.referencePoint
         .translate(key.leftVector,  keyPlateSize.x / 2 + leftOffset)
         .translate(key.frontVector, keyPlateSize.y / 2 + frontOffset)
         .translate(key.bottomVector, bottomOffset),
      Size3d(
         keyPlateSize.x + leftOffset + rightOffset,
         keyPlateSize.y + frontOffset + backOffset,
         height + bottomOffset),
      key.frontVector,
      key.bottomVector
   ))
}

internal fun ScadParentObject.thumbPlateHole(
   thumbPlate: ThumbPlate,
   height: Size,
   bottomOffset: Size = 0.mm,
   otherOffsets: Size = 0.mm
): ScadObject {
   val keyLength = thumbPlate.keySwitches
      .maxOf { ThumbPlate.KEY_PLATE_SIZE.y * it.layoutSize.y }
   val outerRadius = thumbPlate.layoutRadius + keyLength / 2
   val innerRadius = thumbPlate.layoutRadius - keyLength / 2

   val backAngle = Angle.PI / 2

   val startAngle = backAngle - thumbPlate.keyAngle * (thumbPlate.keySwitches.size - 0.5)
   val endAngle = backAngle + thumbPlate.keyAngle / 2

   return place(thumbPlate) {
      translate(y = -thumbPlate.layoutRadius, z = -bottomOffset) {
         difference {
            arcCylinder(
               innerRadius - otherOffsets,
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
