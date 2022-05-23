package com.wcaokaze.ninja60.case.scad

import com.wcaokaze.scadwriter.linearalgebra.*
import com.wcaokaze.ninja60.case.*
import com.wcaokaze.ninja60.parts.key.*
import com.wcaokaze.ninja60.parts.key.alphanumeric.*
import com.wcaokaze.ninja60.parts.key.thumb.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.ninja60.shared.scadutil.*
import com.wcaokaze.ninja60.shared.scadutil.Cube
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

internal fun ScadParentObject.thumbKeyCase(
   case: Case,
   keyTopOffset: Size = 0.mm,
   otherOffsets: Size = 0.mm
): ScadObject {
   return (
      union {
         distortedCube(
            thumbHomeKeyCaseTopPlane(case, otherOffsets),
            thumbHomeKeyCaseLeftPlane(case.thumbHomeKey, otherOffsets),
            thumbHomeKeyCaseBackPlane(case, otherOffsets),
            thumbHomeKeyCaseRightPlane(case.thumbHomeKey, keyTopOffset),
            thumbHomeKeyCaseFrontPlane(case, otherOffsets),
            thumbHomeKeyCaseBottomPlane(case, offset = 0.mm)
         )

         thumbPlateOuterArc(
            case.thumbPlate,
            height = KeySwitch.TRAVEL + keyTopOffset,
            bottomOffset = 20.mm + otherOffsets,
            otherOffsets = otherOffsets
         )

         marginFiller(case, keyTopOffset, surfaceOffsets = otherOffsets,
            internalOffsets = 0.1.mm)
      }
      - thumbPlateInnerArc(
         case.thumbPlate,
         height = KeySwitch.TRAVEL + keyTopOffset,
         bottomOffset = 20.mm + otherOffsets,
         otherOffsets = otherOffsets
      )
      intersection hugeCube(
         bottomPlane = thumbKeyCaseBottomPlane(case, offset = 0.mm)
      )
   )
}

internal fun thumbKeyCaseBottomPlane(case: Case, offset: Size): Plane3d
      = thumbHomeKeyCaseBottomPlane(case, offset)

internal fun thumbHomeKeyCaseLeftPlane(
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

internal fun thumbHomeKeyCaseRightPlane(
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

internal fun thumbHomeKeyCaseFrontPlane(
   case: Case,
   offset: Size
): Plane3d {
   val plateTopLeftPoint = thumbPlateEndPoint(
      case.thumbPlate,
      zOffset = KeySwitch.TRAVEL,
      otherOffsets = offset
   )

   val plateBottomLeftPoint = (
         thumbKeyCaseBottomPlane(case, offset = 0.mm)
         intersection Line3d(plateTopLeftPoint, case.thumbPlate.bottomVector)
   )

   return listOf(plateTopLeftPoint, plateBottomLeftPoint)
      .map { Plane3d(it, case.thumbHomeKey.frontVector) }
      .maxWithOrNull(Plane3d::compareTo)!!
}

internal fun thumbHomeKeyCaseBackPlane(
   case: Case,
   offset: Size
): Plane3d {
   return alphanumericFrontPlaneRight(
      case.alphanumericPlate,
      case.thumbHomeKey,
      offset
   )
}

internal fun thumbHomeKeyCaseBottomPlane(
   case: Case,
   offset: Size
): Plane3d = alphanumericBottomPlane(case, offset)

internal fun thumbHomeKeyCaseTopPlane(
   case: Case,
   offset: Size
): Plane3d {
   return frontRotaryEncoderKeyCaseBottomPlane(case.frontRotaryEncoderKey, offset)
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

private fun thumbPlateStartAngle(thumbPlate: ThumbPlate)
      = -thumbPlate.keyAngle * (thumbPlate.keySwitches.size - 0.5)
private fun thumbPlateEndAngle(thumbPlate: ThumbPlate)
      = thumbPlate.keyAngle / 2

private fun thumbPlateEndPoint(
   thumbPlate: ThumbPlate,
   zOffset: Size,
   otherOffsets: Size
): Point3d {
   val keyLength = thumbPlate.keySwitches
      .maxOf { ThumbPlate.KEY_PLATE_SIZE.y * it.layoutSize.y }

   return arcEndPoint(
         origin = thumbPlate.referencePoint
            .translate(thumbPlate.frontVector, thumbPlate.layoutRadius),
         xAxis = thumbPlate.backVector,
         yAxis = thumbPlate.leftVector,
         arcRadius = thumbPlate.layoutRadius - keyLength / 2 - otherOffsets,
         angle = thumbPlateEndAngle(thumbPlate),
         otherOffsets
      )
      .translate(thumbPlate.topVector, zOffset)
}

internal fun thumbPlateLeftPlane(
   thumbPlate: ThumbPlate,
   offset: Size
) = Plane3d(
   thumbPlateEndPoint(thumbPlate, zOffset = 0.mm, offset),
   thumbPlate.backVector
      .rotate(thumbPlate.topVector, thumbPlateEndAngle(thumbPlate) + 90.deg)
)

internal fun thumbPlateTopPlane(
   thumbPlate: ThumbPlate,
   offset: Size
) = Plane3d(
   thumbPlate.referencePoint
      .translate(thumbPlate.topVector, KeySwitch.TRAVEL + offset),
   thumbPlate.topVector
)

internal fun ScadParentObject.thumbPlateHole(
   thumbPlate: ThumbPlate,
   height: Size,
   bottomOffset: Size = 0.mm,
   otherOffsets: Size = 0.mm
): ScadObject {
   return difference {
      thumbPlateOuterArc(thumbPlate, height, bottomOffset, otherOffsets)
      thumbPlateInnerArc(thumbPlate, height, bottomOffset, otherOffsets)
   }
}

private fun ScadParentObject.thumbPlateOuterArc(
   thumbPlate: ThumbPlate,
   height: Size,
   bottomOffset: Size,
   otherOffsets: Size,
): ScadObject {
   val keyLength = thumbPlate.keySwitches
      .maxOf { ThumbPlate.KEY_PLATE_SIZE.y * it.layoutSize.y }
   val radius = thumbPlate.layoutRadius + keyLength / 2

   val backAngle = Angle.PI / 2

   return place(thumbPlate) {
      translate(y = -thumbPlate.layoutRadius, z = -bottomOffset) {
         arcCylinder(
            radius + otherOffsets,
            height + bottomOffset,
            backAngle + thumbPlateStartAngle(thumbPlate),
            backAngle + thumbPlateEndAngle(thumbPlate),
            otherOffsets
         )
      }
   }
}

private fun ScadParentObject.thumbPlateInnerArc(
   thumbPlate: ThumbPlate,
   height: Size,
   bottomOffset: Size,
   otherOffsets: Size,
): ScadObject {
   val keyLength = thumbPlate.keySwitches
      .maxOf { ThumbPlate.KEY_PLATE_SIZE.y * it.layoutSize.y }
   val radius = thumbPlate.layoutRadius - keyLength / 2

   val backAngle = Angle.PI / 2

   return place(thumbPlate) {
      translate(y = -thumbPlate.layoutRadius, z = -bottomOffset - 0.01.mm) {
         arcCylinder(
            radius - otherOffsets,
            height + bottomOffset + 0.02.mm,
            backAngle + thumbPlateStartAngle(thumbPlate) - 0.1.deg,
            backAngle + thumbPlateEndAngle(thumbPlate) + 0.1.deg,
            otherOffsets
         )
      }
   }
}

/**
 * [AlphanumericPlate]と[ThumbPlate]の隙間などを埋めるやつ
 *
 * @param surfaceOffsets
 * [AlphanumericPlate]など他のパーツと接触せず、壁として露出する部分のオフセット
 * @param internalOffsets
 * [AlphanumericPlate]など他のパーツと接触している部分のオフセット
 */
private fun ScadParentObject.marginFiller(
   case: Case,
   keyTopOffset: Size,
   surfaceOffsets: Size,
   internalOffsets: Size
): ScadObject {
   return union {
      distortedCube(
         leftPlane = thumbHomeKeyCaseRightPlane(
            case.thumbHomeKey, offset = keyTopOffset - internalOffsets),
         rightPlane = thumbPlateLeftPlane(case.thumbPlate, offset = -internalOffsets),
         frontPlane = thumbHomeKeyCaseFrontPlane(case, offset = surfaceOffsets),
         backPlane = thumbHomeKeyCaseBackPlane(case, offset = internalOffsets),
         bottomPlane = thumbKeyCaseBottomPlane(case, offset = internalOffsets),
         topPlane = thumbPlateTopPlane(case.thumbPlate, offset = keyTopOffset)
      )

      distortedCube(
         leftPlane = thumbHomeKeyCaseRightPlane(
            case.thumbHomeKey, offset = keyTopOffset - internalOffsets),
         rightPlane = alphanumericRightPlane(case.alphanumericPlate, surfaceOffsets),
         frontPlane = alphanumericFrontPlaneRight(
            case.alphanumericPlate, case.thumbHomeKey, offset = 20.mm),
         backPlane = alphanumericFrontPlaneRight(
            case.alphanumericPlate, case.thumbHomeKey, offset = -internalOffsets),
         bottomPlane = thumbKeyCaseBottomPlane(case, offset = internalOffsets),
         topPlane = thumbPlateTopPlane(case.thumbPlate, offset = keyTopOffset)
      )
   }
}
