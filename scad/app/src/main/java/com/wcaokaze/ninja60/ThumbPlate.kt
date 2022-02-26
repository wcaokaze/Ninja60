package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * 親指で押すキーのうち、指の側面で押す方の扇形に並んだキースイッチを挿すためのプレート。
 * 指の腹で押す方のキーはこのプレートに含まれない
 *
 * [referencePoint]は親指のホームポジションのキーの位置、
 * [frontVector]は[referencePoint]から扇形の中心方向。
 */
data class ThumbPlate(
   override val referencePoint: Point3d,
   val layoutRadius: Size,
   val keyPitch: Size,
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d
) : Transformable<ThumbPlate> {
   companion object {
      val KEY_PLATE_SIZE = Size2d(17.5.mm, 17.5.mm)
   }

   val keySwitches: List<KeySwitch> get() {
      val arcCenter = referencePoint.translate(frontVector, layoutRadius)
      val axis = Line3d(arcCenter, topVector)

      val referenceKeySwitch = KeySwitch(referencePoint, bottomVector, frontVector)
      val a = Angle(keyPitch / layoutRadius)

      return List(3) { referenceKeySwitch.rotate(axis, a * -it) }
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = ThumbPlate(referencePoint, layoutRadius, keyPitch, frontVector, bottomVector)
}

// =============================================================================

/*
/**
 * @param layerOffset
 * 各KeyPlateの位置が[KeySwitch.bottomVector]方向へ移動する
 * @param leftRightOffset
 * 一番左のキーと一番右のキーがさらに左右に広がるが、Ninja60の場合左と右のKeyPlateは
 * 上を向いているので上に広がる
 * @param frontOffset
 * 手前(親指の付け根方向)に広がる
 */
fun ScadParentObject.hullThumbPlate(
   thumbPlate: ThumbPlate,
   layerOffset: Size = 0.mm,
   leftRightOffset: Size = 0.mm,
   frontOffset: Size = 0.mm
): ScadObject {
   val columnSwitches = thumbPlate.column.map { it.translate(it.bottomVector, layerOffset) }
   val columnPlates = columnSwitches.map { it.plate(ThumbPlate.KEY_PLATE_SIZE) }

   val frontWallPlane = thumbPlate.frontPlane
      .let { it.translate(it.normalVector, frontOffset) }

   val backWallPlane = thumbPlate.backPlane
      .let { it.translate(it.normalVector, layerOffset) }

   val leftmostPlate  = columnPlates.first()
   val rightmostPlate = columnPlates.last()

   val boundaryLines = columnBoundaryLines(columnPlates)

   val leftmostLine  = boundaryLines.first().translate(leftmostPlate .leftVector,  leftRightOffset)
   val rightmostLine = boundaryLines.last() .translate(rightmostPlate.rightVector, leftRightOffset)

   val columnPoints = listOf(
         leftmostLine.translate(leftmostPlate.topVector, layerOffset),
         leftmostLine,
         *boundaryLines.drop(1).dropLast(1).toTypedArray(),
         rightmostLine,
         rightmostLine.translate(rightmostPlate.topVector, layerOffset)
      )
      .flatMap {
         listOf(
            backWallPlane  intersection it,
            frontWallPlane intersection it
         )
      }

   return hullPoints(columnPoints)
}

private fun columnBoundaryLines(columnPlates: List<KeyPlate>): List<Line3d> {
   val lines = ArrayList<Line3d>()

   val leftmostPlate = columnPlates.first()
   lines += Line3d(leftmostPlate.backLeft, leftmostPlate.frontLeft)

   for ((left, right) in columnPlates.zipWithNext()) {
      val leftPlane  = Plane3d(left .referencePoint, left .bottomVector)
      val rightPlane = Plane3d(right.referencePoint, right.bottomVector)
      lines += leftPlane intersection rightPlane
   }

   val rightmostPlate = columnPlates.last()
   lines += Line3d(rightmostPlate.backRight, rightmostPlate.frontRight)

   return lines
}

val ThumbPlate.frontPlane: Plane3d get() {
   return Plane3d(
      referencePoint
         .translate(
            frontVector,
            ThumbPlate.KEY_PLATE_SIZE.y * column.maxOf { it.layoutSize.y } / 2
         ),
      frontVector
   )
}

val ThumbPlate.backPlane: Plane3d get() {
   return Plane3d(
      referencePoint
         .translate(
            backVector,
            ThumbPlate.KEY_PLATE_SIZE.y * column.maxOf { it.layoutSize.y } / 2
         ),
      backVector
   )
}
*/
