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

   alphanumericColumns(topPlate.alphanumericColumns)
}

private fun ScadWriter.alphanumericColumns(alphanumericColumns: AlphanumericColumns) {
   val leftmostColumn = alphanumericColumns.columns[0]

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

   val leftmostRightWallPlane = getWallPlane(
      alphanumericColumns.columns[0],
      alphanumericColumns.columns[1]
   )

   // --------

   val rightmostColumn = alphanumericColumns.columns[alphanumericColumns.columns.lastIndex]

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

   val rightmostLeftWallPlane = getWallPlane(
      alphanumericColumns.columns[alphanumericColumns.columns.lastIndex - 1],
      alphanumericColumns.columns[alphanumericColumns.columns.lastIndex]
   )

   // --------

   union {
      hullPoints(
         leftmostColumn.boundaryLines().map { leftmostWallPlane      intersection it } +
         leftmostColumn.boundaryLines().map { leftmostRightWallPlane intersection it }
      )

      for ((left, column, right) in alphanumericColumns.columns.windowed(3)) {
         val leftWallPlane  = getWallPlane(left, column)
         val rightWallPlane = getWallPlane(column, right)

         hullPoints(
            column.boundaryLines().map { leftWallPlane  intersection it } +
            column.boundaryLines().map { rightWallPlane intersection it }
         )
      }

      hullPoints(
         rightmostColumn.boundaryLines().map { rightmostLeftWallPlane intersection it } +
         rightmostColumn.boundaryLines().map { rightmostWallPlane     intersection it }
      )
   }
}

/** このColumnの各KeyPlate同士の境界線(最上段の奥のフチと最下段の手前のフチを含む) */
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
