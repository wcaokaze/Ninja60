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
      alphanumericColumns(topPlate.alphanumericColumns, layerOffset = 1.5.mm, frontBackOffset =  1.5.mm, leftRightOffset = 1.mm)
      alphanumericColumns(topPlate.alphanumericColumns, layerOffset = 0.0.mm, frontBackOffset = 20.0.mm, leftRightOffset = 0.mm)

      topPlate.alphanumericColumns.columns
         .flatMap { it.keyPlates }
         .map { it.copy(size = Size2d(14.mm, 14.mm)) }
         .map { keyPlate ->
            keyPlate.points +
                  keyPlate.points.map {
                     it.translate(keyPlate.normalVector.toUnitVector() * -2)
                  }
         }
         .forEach { hullPoints(it) }
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

   fun ScadWriter.column(
      column: Column,
      leftWallPlane: Plane3d,
      rightWallPlane: Plane3d
   ) {
      val mostBackPlate  = column.keyPlates.first()
      val mostFrontPlate = column.keyPlates.last()

      val boundaryLines = column.boundaryLines()

      val mostBackLine  = boundaryLines.first().translate(mostBackPlate .frontVector.toUnitVector() * -frontBackOffset.numberAsMilliMeter)
      val mostFrontLine = boundaryLines.last() .translate(mostFrontPlate.frontVector.toUnitVector() *  frontBackOffset.numberAsMilliMeter)

      val mostBackLayeredLine  = mostBackLine .translate(mostBackPlate .normalVector.toUnitVector() * 20)
      val mostFrontLayeredLine = mostFrontLine.translate(mostFrontPlate.normalVector.toUnitVector() * 20)

      val lines = listOf(
         mostBackLayeredLine,
         mostBackLine,
         *boundaryLines.drop(1).dropLast(1).toTypedArray(),
         mostFrontLine,
         mostFrontLayeredLine
      )

      hullPoints(
         lines.map { leftWallPlane  intersection it } +
         lines.map { rightWallPlane intersection it }
      )
   }

   // --------

   union {
      column(leftmostColumn, leftmostWallPlane, leftmostRightWallPlane)

      for ((left, column, right) in columns.windowed(3)) {
         column(
            column,
            getWallPlane(left, column) .translateByNormalVector(-leftRightOffset),
            getWallPlane(column, right).translateByNormalVector( leftRightOffset)
         )
      }

      column(rightmostColumn, rightmostLeftWallPlane, rightmostWallPlane)
   }
}

/**
 * このColumnの各KeyPlate同士の境界線(最上段の奥のフチと最下段の手前のフチを含む)
 */
fun Column.boundaryLines(): List<Line3d> {
   val lines = ArrayList<Line3d>()

   val mostBackPlate = keyPlates.first()
   lines += Line3d(mostBackPlate.backLeft, mostBackPlate.backRight)

   for ((back, front) in keyPlates.zipWithNext()) {
      val backPlane  = Plane3d(back .center, back .normalVector)
      val frontPlane = Plane3d(front.center, front.normalVector)
      lines += backPlane intersection frontPlane
   }

   val mostFrontPlate = keyPlates.last()
   lines += Line3d(mostFrontPlate.frontLeft, mostFrontPlate.frontRight)

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
