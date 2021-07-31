package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

data class ThumbPlate(
   val thumbKeys: ThumbKeys
) {
   companion object {
      operator fun invoke() = ThumbPlate(
         ThumbKeys(
            referencePoint = Point3d.ORIGIN,
            bottomVector = -Vector3d.Z_UNIT_VECTOR,
            alignmentVector = -Vector3d.Y_UNIT_VECTOR,
            radius = 16.mm,
            keySize = KeyPlate.SIZE.copy(y = 22.4.mm),
            layerDistance = -Keycap.THICKNESS - KeySwitch.STEM_HEIGHT - KeySwitch.TOP_HEIGHT
         )
      )
   }
}

fun ScadWriter.thumbPlate() {
   val plate = ThumbPlate()

   difference {
      //                     layerOffset, leftRightOffset, frontOffset
      thumbKeys(plate.thumbKeys, 1.5.mm,          1.5.mm,      1.5.mm)
      thumbKeys(plate.thumbKeys, 0.0.mm,         20.0.mm,     20.0.mm)

      plate.thumbKeys.column
         .plus(plate.thumbKeys.backKey)
         .map { it.copy(size = Size2d(14.mm, 14.mm)) }
         .map { keyPlate ->
            keyPlate.points +
                  keyPlate.points.map { it.translate(keyPlate.normalVector, (-2).mm) }
         }
         .forEach { hullPoints(it) }
   }
}

/**
 * @param layerOffset
 * [layerDistance][ThumbKeys.layerDistance]が足される
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

   val layeredThumbKeys = thumbKeys.copy(layerDistance = thumbKeys.layerDistance - layerOffset)

   val frontWallPlane = Plane3d(
         layeredThumbKeys.column
            .flatMap { listOf(it.frontLeft, it.frontRight) }
            .minByOrNull {
               it.rotate(
                  Line3d.Z_AXIS,
                  layeredThumbKeys.alignmentVector.copy(z = 0.mm) angleWith Vector3d.Y_UNIT_VECTOR
               ).y
            } !!,
         layeredThumbKeys.alignmentVector
      )
      .translateByNormalVector(frontOffset)

   val backWallPlane = Plane3d(
      layeredThumbKeys.backKey.center,
      layeredThumbKeys.backKey.normalVector
   )

   val leftmostPlate  = layeredThumbKeys.column.first()
   val rightmostPlate = layeredThumbKeys.column.last()

   val boundaryLines = layeredThumbKeys.columnBoundaryLines()

   fun KeyPlate.rightVector() = normalVector vectorProduct frontVector
   val leftmostLine  = boundaryLines.first().translate(leftmostPlate .rightVector(), -leftRightOffset)
   val rightmostLine = boundaryLines.last() .translate(rightmostPlate.rightVector(),  leftRightOffset)

   val columnPoints = listOf(
         leftmostLine.translate(leftmostPlate.normalVector, 1.5.mm),
         leftmostLine,
         *boundaryLines.drop(1).dropLast(1).toTypedArray(),
         rightmostLine,
         rightmostLine.translate(rightmostPlate.normalVector, 1.5.mm)
      )
      .flatMap {
         listOf(
            backWallPlane  intersection it,
            frontWallPlane intersection it
         )
      }

   val backPlatePoints = layeredThumbKeys.backKey.points
      .flatMap { point ->
         listOf(
            point,
            point.translate(layeredThumbKeys.backKey.normalVector, 1.5.mm),
         )
      }

   hullPoints(
      *columnPoints.toTypedArray(),
      *backPlatePoints.toTypedArray(),
   )
}

/**
 * このThumbKeysの[column][ThumbKeys.column]の各KeyPlate
 */
fun ThumbKeys.columnBoundaryLines(): List<Line3d> {
   val lines = ArrayList<Line3d>()

   val leftmostPlate = column.first()
   lines += Line3d(leftmostPlate.backLeft, leftmostPlate.frontLeft)

   for ((left, right) in column.zipWithNext()) {
      val leftPlane  = Plane3d(left .center, left .normalVector)
      val rightPlane = Plane3d(right.center, right.normalVector)
      lines += leftPlane intersection rightPlane
   }

   val rightmostPlate = column.last()
   lines += Line3d(rightmostPlate.backRight, rightmostPlate.frontRight)

   return lines
}
