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
   homeKeyTopOffset: Size = 0.mm,
   otherOffsets: Size = 0.mm
): ScadObject {
   return (
      distortedCube(
         thumbHomeKeyCaseTopPlane(case, otherOffsets),
         thumbHomeKeyCaseLeftPlane(case.thumbHomeKey, otherOffsets),
         thumbHomeKeyCaseBackPlane(case, otherOffsets),
         thumbHomeKeyCaseRightPlane(case.thumbHomeKey, homeKeyTopOffset),
         thumbHomeKeyCaseFrontPlane(case, otherOffsets),
         thumbHomeKeyCaseBottomPlane(case, offset = 0.mm)
      )
      + thumbPlateOuterArc(
         case.thumbPlate,
         height = KeySwitch.TRAVEL,
         bottomOffset = 20.mm + otherOffsets,
         otherOffsets = otherOffsets
      )
      - thumbPlateInnerArc(
         case.thumbPlate,
         height = KeySwitch.TRAVEL,
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
         angle = thumbPlate.keyAngle / 2,
         otherOffsets
      )
      .translate(thumbPlate.topVector, zOffset)
}

/**
 * *指定した平面における* 円弧の終点を返す。
 * @param origin 平面の原点とみなす点。[Point3d.ORIGIN]である必要はない
 * @param xAxis 平面のX軸とみなす向き。[Vector3d.X_UNIT_VECTOR]である必要はない
 * @param yAxis 平面のY軸とみなす向き。[Vector3d.Y_UNIT_VECTOR]である必要はない
 */
private fun arcEndPoint(
   origin: Point3d, xAxis: Vector3d, yAxis: Vector3d,
   arcRadius: Size, angle: Angle, offset: Size
): Point3d {
   val zAxis = xAxis vectorProduct yAxis
   fun vector(angle: Angle) = xAxis.rotate(zAxis, angle)

   return origin
      .translate(vector(angle), arcRadius)
      .translate(vector(angle + 90.deg), offset)
}

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

   val startAngle = backAngle - thumbPlate.keyAngle * (thumbPlate.keySwitches.size - 0.5)
   val endAngle = backAngle + thumbPlate.keyAngle / 2

   return place(thumbPlate) {
      translate(y = -thumbPlate.layoutRadius, z = -bottomOffset) {
         arcCylinder(
            radius + otherOffsets,
            height + bottomOffset,
            startAngle,
            endAngle,
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

   val startAngle = backAngle - thumbPlate.keyAngle * (thumbPlate.keySwitches.size - 0.5)
   val endAngle = backAngle + thumbPlate.keyAngle / 2

   return place(thumbPlate) {
      translate(y = -thumbPlate.layoutRadius, z = -bottomOffset - 0.01.mm) {
         arcCylinder(
            radius - otherOffsets,
            height + bottomOffset + 0.02.mm,
            startAngle - 0.1.deg,
            endAngle + 0.1.deg,
            otherOffsets
         )
      }
   }
}
