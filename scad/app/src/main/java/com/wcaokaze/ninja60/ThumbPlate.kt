package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

data class ThumbPlate(
   val thumbKeys: ThumbKeys
) {
   companion object {
      val COLUMN_KEY_PLATE_SIZE = Size2d(17.5.mm, 22.4.mm)
      val BACK_KEY_PLATE_SIZE   = Size2d(17.5.mm, 17.5.mm)

      operator fun invoke() = ThumbPlate(
         ThumbKeys(
            referencePoint = Point3d.ORIGIN,
            bottomVector = -Vector3d.Z_UNIT_VECTOR,
            frontVector = -Vector3d.Y_UNIT_VECTOR,
            radius = 16.mm
         )
      )
   }
}

fun ThumbPlate.translate(distance: Size3d) = ThumbPlate(
   thumbKeys.translate(distance)
)

fun ThumbPlate.translate(distance: Vector3d) = ThumbPlate(
   thumbKeys.translate(distance)
)

fun ThumbPlate.translate(direction: Vector3d, distance: Size) = ThumbPlate(
   thumbKeys.translate(direction, distance)
)

fun ThumbPlate.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm
): ThumbPlate = translate(Size3d(x, y, z))

fun ThumbPlate.rotate(axis: Line3d, angle: Angle) = ThumbPlate(
   thumbKeys.rotate(axis, angle)
)

fun ScadWriter.thumbPlate(thumbPlate: ThumbPlate) {
   difference {
      val tk = thumbPlate.thumbKeys

      //                               layerOffset, leftRightOffset, frontOffset
      thumbKeys(tk, KeySwitch.BOTTOM_HEIGHT - 1.mm,          1.5.mm,      1.5.mm)
      thumbKeys(tk,                           0.mm,         20.0.mm,     20.0.mm)

      for (k in tk.column + tk.backKey) {
         switchHole(k)
      }
   }
}

/**
 * @param layerOffset
 * 各KeyPlateの位置が[KeySwitch.bottomVector]方向へ移動する
 * @param leftRightOffset
 * 一番左のキーと一番右のキーがさらに左右に広がるが、Ninja60の場合左と右のKeyPlateは
 * 上を向いているので上に広がる
 * @param frontOffset
 * 手前(親指の付け根方向)に広がる
 */
private fun ScadWriter.thumbKeys(
   thumbKeys: ThumbKeys,
   layerOffset: Size,
   leftRightOffset: Size,
   frontOffset: Size
) {
   fun Plane3d.translateByNormalVector(size: Size): Plane3d {
      return translate(normalVector, size)
   }

   val columnSwitches = thumbKeys.column.map { it.translate(it.bottomVector, layerOffset) }
   val columnPlates = columnSwitches
      .map { KeyPlate(it.center, ThumbPlate.COLUMN_KEY_PLATE_SIZE, -it.bottomVector, it.frontVector) }
   val backKeySwitch = thumbKeys.backKey.translate(thumbKeys.backKey.bottomVector, layerOffset)
   val backKeyPlate = KeyPlate(backKeySwitch.center, ThumbPlate.BACK_KEY_PLATE_SIZE,
      -backKeySwitch.bottomVector, backKeySwitch.frontVector)

   val frontWallPlane = Plane3d(
         thumbKeys.referencePoint.translate(thumbKeys.frontVector, ThumbPlate.COLUMN_KEY_PLATE_SIZE.y / 2),
         thumbKeys.frontVector
      )
      .translateByNormalVector(frontOffset)

   val backWallPlane = Plane3d(backKeySwitch.center, -backKeySwitch.bottomVector)

   val leftmostPlate  = columnPlates.first()
   val rightmostPlate = columnPlates.last()

   val boundaryLines = columnBoundaryLines(columnPlates)

   fun KeyPlate.rightVector() = normalVector vectorProduct frontVector
   val leftmostLine  = boundaryLines.first().translate(leftmostPlate .rightVector(), -leftRightOffset)
   val rightmostLine = boundaryLines.last() .translate(rightmostPlate.rightVector(),  leftRightOffset)

   val columnPoints = listOf(
         leftmostLine.translate(leftmostPlate.normalVector, layerOffset),
         leftmostLine,
         *boundaryLines.drop(1).dropLast(1).toTypedArray(),
         rightmostLine,
         rightmostLine.translate(rightmostPlate.normalVector, layerOffset)
      )
      .flatMap {
         listOf(
            backWallPlane  intersection it,
            frontWallPlane intersection it
         )
      }

   val backPlatePoints = backKeyPlate.points
      .flatMap { point ->
         listOf(
            point,
            point.translate(backKeyPlate.normalVector, layerOffset),
         )
      }

   hullPoints(
      *columnPoints.toTypedArray(),
      *backPlatePoints.toTypedArray(),
   )
}

private fun columnBoundaryLines(columnPlates: List<KeyPlate>): List<Line3d> {
   val lines = ArrayList<Line3d>()

   val leftmostPlate = columnPlates.first()
   lines += Line3d(leftmostPlate.backLeft, leftmostPlate.frontLeft)

   for ((left, right) in columnPlates.zipWithNext()) {
      val leftPlane  = Plane3d(left .center, left .normalVector)
      val rightPlane = Plane3d(right.center, right.normalVector)
      lines += leftPlane intersection rightPlane
   }

   val rightmostPlate = columnPlates.last()
   lines += Line3d(rightmostPlate.backRight, rightmostPlate.frontRight)

   return lines
}
