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

   val points = topPlate.alphanumericColumns.points()

   for ((leftPoints, rightPoints) in points.zipWithNext()) {
      for ((backPoints, frontPoints) in (leftPoints zip rightPoints).zipWithNext()) {
         val (backLeft, backRight) = backPoints
         val (frontLeft, frontRight) = frontPoints

         hullPoints(backLeft, backRight, frontRight, frontLeft)
      }
   }
}

private fun AlphanumericColumns.points(): List<List<Point3d>> {
   val points = ArrayList<List<Point3d>>()

   val leftmostColumn = columns.first()

   val leftmostWall = Plane3d(
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

   points += leftmostColumn.boundaryLines().map { leftmostWall intersection it }

   for ((left, right) in columns.zipWithNext()) {
      val wallPlane = getWallPlane(left, right)
      points += left .boundaryLines().map { wallPlane intersection it }
      points += right.boundaryLines().map { wallPlane intersection it }
   }

   val rightmostColumn = columns.last()

   val rightmostWall = Plane3d(
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

   points += rightmostColumn.boundaryLines().map { rightmostWall intersection it }

   return points
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
