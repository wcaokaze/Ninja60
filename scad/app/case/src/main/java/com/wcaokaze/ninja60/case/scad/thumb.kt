package com.wcaokaze.ninja60.case.scad

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.case.*
import com.wcaokaze.ninja60.parts.key.*
import com.wcaokaze.ninja60.parts.key.thumb.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.ninja60.shared.scadutil.*
import com.wcaokaze.ninja60.shared.scadutil.Cube
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

internal fun ScadParentObject.thumbKeyCase(
   case: Case,
   offsets: Size = 0.mm
): ScadObject {
   return intersection {
      hugeCube(
         leftPlane = thumbKeyCaseLeftPlane(case.thumbHomeKey, offsets),
         backPlane = thumbKeyCaseBackPlane(case, offsets),
         bottomPlane = thumbKeyCaseBottomPlane(case, offset = 0.mm),
         topPlane = thumbKeyCaseTopPlane(case, offsets)
      )

      union {
         distortedCube(
            thumbKeyCaseTopPlane(case, offsets),
            thumbKeyCaseLeftPlane(case.thumbHomeKey, offsets),
            thumbKeyCaseBackPlane(case, offsets),
            thumbKeyCaseRightPlane(case.thumbHomeKey, offsets),
            thumbKeyCaseFrontPlane(case.thumbHomeKey, offsets),
            thumbKeyCaseBottomPlane(case, offset = 0.mm)
         )

         thumbPlateHole(
            case.thumbPlate,
            height = KeySwitch.TRAVEL,
            bottomOffset = 20.mm + offsets,
            otherOffsets = offsets
         )
      }
   }
}

internal fun thumbKeyCaseLeftPlane(
   thumbHomeKey: KeySwitch,
   offset: Size
): Plane3d {
   return Plane3d(
         thumbHomeKey.referencePoint
            .translate(
               thumbHomeKey.bottomVector,
               Case.THUMB_HOME_KEY_CASE_HEIGHT
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

internal fun thumbKeyCaseFrontPlane(
   thumbHomeKey: KeySwitch,
   offset: Size
): Plane3d {
   return Plane3d(
         thumbHomeKey.referencePoint
            .translate(
               thumbHomeKey.frontVector,
               ThumbPlate.KEY_PLATE_SIZE.y / 2
            ),
         thumbHomeKey.frontVector
      )
      .translateNormalVector(offset)
}

internal fun thumbKeyCaseBackPlane(
   case: Case,
   offset: Size
): Plane3d {
   return alphanumericFrontPlaneRight(
      case.alphanumericPlate,
      case.thumbHomeKey,
      offset
   )
}

internal fun thumbKeyCaseBottomPlane(
   case: Case,
   offset: Size
): Plane3d = alphanumericBottomPlane(case, offset)

internal fun thumbKeyCaseTopPlane(
   case: Case,
   offset: Size
): Plane3d {
   return frontRotaryEncoderKeyCaseBottomPlane(case.frontRotaryEncoderKey)
      .translateNormalVector(offset)
}

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
