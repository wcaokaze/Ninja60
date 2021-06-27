package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

val topPlateThickness = 1.5.mm
val topPlateHeight = 5.mm - topPlateThickness

val topPlateHoleSize = Size2d(14.mm, 14.mm)

data class TopPlate(
   val alphanumericColumns: AlphanumericColumns
) {
   companion object {
      operator fun invoke() = TopPlate(
         AlphanumericColumns(-Keycap.THICKNESS - KeySwitch.STEM_HEIGHT - KeySwitch.TOP_HEIGHT)
      )
   }
}

fun ScadWriter.topPlate() {
   val topPlate = TopPlate()

   difference {
      alphanumericColumns(topPlate.alphanumericColumns, layerOffset = 1.5.mm, frontBackOffset = 0.mm, leftRightOffset = 1.5.mm)
      alphanumericColumns(topPlate.alphanumericColumns, layerOffset = 0.0.mm, frontBackOffset = 5.mm, leftRightOffset = 0.0.mm)
   }
}

/**
 * @param layerOffset
 * 各Columnの[layerDistance][Column.layerDistance]が足される。つまり各Column深くなる
 * @param frontBackOffset
 * [Column.boundaryLines]に渡されるoffset。各Column手前と奥に広がる
 * が、Ninja60の場合手前と奥のKeyPlateは上を向いているので上に広がる
 * @param leftRightOffset
 * 各Column 左右方向に広がる
 */
private fun ScadWriter.alphanumericColumns(
   alphanumericColumns: AlphanumericColumns,
   layerOffset: Size,
   frontBackOffset: Size,
   leftRightOffset: Size
) {
   fun Plane3d.translateByNormalVector(size: Size): Plane3d {
      return translate(normalVector.toUnitVector() * size.numberAsMilliMeter)
   }

   val columns = alphanumericColumns.columns.map { it.copy(layerDistance = it.layerDistance - layerOffset) }
   val leftmostColumn = columns[0]

   val leftmostWallPlane = Plane3d(
         leftmostColumn.keyPlates
            .flatMap { listOf(it.backLeft, it.frontLeft) }
            .minByOrNull {
               // いやこのへんのコード汚すぎでしょ
               // とりあえずColumnから見て一番左の座標が知りたいだけなので
               // 真面目に回転せずalignmentVectorのX成分がゼロになればいいくらいの感じ
               it.rotate(
                  Line3d.Z_AXIS,
                  leftmostColumn.alignmentVector.copy(z = 0.mm) angleWith Vector3d.Y_UNIT_VECTOR
               ).x
            } !!,
         leftmostColumn.alignmentVector vectorProduct leftmostColumn.bottomVector
      )
      .translateByNormalVector(-frontBackOffset)

   val leftmostRightWallPlane = getWallPlane(columns[0], columns[1])
      .translateByNormalVector(leftRightOffset)

   // --------

   val rightmostColumn = columns[columns.lastIndex]

   val rightmostWallPlane = Plane3d(
         rightmostColumn.keyPlates
            .flatMap { listOf(it.backRight, it.frontRight) }
            .maxByOrNull {
               it.rotate(
                  Line3d.Z_AXIS,
                  rightmostColumn.alignmentVector.copy(z = 0.mm) angleWith Vector3d.Y_UNIT_VECTOR
               ).x
            } !!,
         rightmostColumn.alignmentVector vectorProduct rightmostColumn.bottomVector
      )
      .translateByNormalVector(frontBackOffset)

   val rightmostLeftWallPlane = getWallPlane(columns[columns.lastIndex - 1], columns[columns.lastIndex])
      .translateByNormalVector(-leftRightOffset)

   // --------

   union {
      hullPoints(
         leftmostColumn.boundaryLines(frontBackOffset).map { leftmostWallPlane      intersection it } +
         leftmostColumn.boundaryLines(frontBackOffset).map { leftmostRightWallPlane intersection it }
      )

      for ((left, column, right) in columns.windowed(3)) {
         val leftWallPlane  = getWallPlane(left, column) .translateByNormalVector(-leftRightOffset)
         val rightWallPlane = getWallPlane(column, right).translateByNormalVector( leftRightOffset)

         hullPoints(
            column.boundaryLines(frontBackOffset).map { leftWallPlane  intersection it } +
            column.boundaryLines(frontBackOffset).map { rightWallPlane intersection it }
         )
      }

      hullPoints(
         rightmostColumn.boundaryLines(frontBackOffset).map { rightmostLeftWallPlane intersection it } +
         rightmostColumn.boundaryLines(frontBackOffset).map { rightmostWallPlane     intersection it }
      )
   }
}

/**
 * このColumnの各KeyPlate同士の境界線(最上段の奥のフチと最下段の手前のフチを含む)
 *
 * @param offset
 * 一番奥の境界線がさらに奥に、一番手前の境界線がさらに手前に移動する
 */
fun Column.boundaryLines(offset: Size): List<Line3d> {
   val lines = ArrayList<Line3d>()

   val mostBackPlate = keyPlates.first()
   lines += Line3d(mostBackPlate.backLeft, mostBackPlate.backRight)
      .translate(mostBackPlate.frontVector.toUnitVector() * -offset.numberAsMilliMeter)

   for ((back, front) in keyPlates.zipWithNext()) {
      val backPlane  = Plane3d(back .center, back .normalVector)
      val frontPlane = Plane3d(front.center, front.normalVector)
      lines += backPlane intersection frontPlane
   }

   val mostFrontPlate = keyPlates.last()
   lines += Line3d(mostFrontPlate.frontLeft, mostFrontPlate.frontRight)
      .translate(mostFrontPlate.frontVector.toUnitVector() * offset.numberAsMilliMeter)

   return lines
}

private fun getWallPlane(leftColumn: Column, rightColumn: Column): Plane3d {
   val alignmentVector = run {
      val (lx, ly, lz) = leftColumn .alignmentVector
      val (rx, ry, rz) = rightColumn.alignmentVector
      Vector3d((lx + rx) / 2, (ly + ry) / 2, (lz + rz) / 2)
   }

   val bottomVector = run {
      val (lx, ly, lz) = leftColumn .bottomVector
      val (rx, ry, rz) = rightColumn.bottomVector
      Vector3d((lx + rx) / 2, (ly + ry) / 2, (lz + rz) / 2)
   }

   val referencePoint = run {
      val (lx, ly, lz) = leftColumn .referencePoint
      val (rx, ry, rz) = rightColumn.referencePoint

      Point3d(
         Point((lx.distanceFromOrigin + rx.distanceFromOrigin) / 2),
         Point((ly.distanceFromOrigin + ry.distanceFromOrigin) / 2),
         Point((lz.distanceFromOrigin + rz.distanceFromOrigin) / 2)
      )
   }

   return Plane3d(referencePoint, alignmentVector vectorProduct bottomVector)
}
