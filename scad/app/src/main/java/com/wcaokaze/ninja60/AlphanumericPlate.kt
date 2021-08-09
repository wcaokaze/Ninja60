package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

data class AlphanumericPlate(
   val alphanumericColumns: AlphanumericColumns
) {
   companion object {
      val KEY_PLATE_SIZE = Size2d(17.5.mm, 17.5.mm)

      operator fun invoke() = AlphanumericPlate(
         AlphanumericColumns()
      )
   }
}

fun AlphanumericPlate.translate(distance: Size3d) = AlphanumericPlate(
   alphanumericColumns.translate(distance)
)

fun AlphanumericPlate.translate(distance: Vector3d) = AlphanumericPlate(
   alphanumericColumns.translate(distance)
)

fun AlphanumericPlate.translate(direction: Vector3d, distance: Size) = AlphanumericPlate(
   alphanumericColumns.translate(direction, distance)
)

fun AlphanumericPlate.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm
): AlphanumericPlate = translate(Size3d(x, y, z))

fun AlphanumericPlate.rotate(axis: Line3d, angle: Angle) = AlphanumericPlate(
   alphanumericColumns.rotate(axis, angle)
)

fun ScadWriter.alphanumericPlate(alphanumericPlate: AlphanumericPlate) {
   difference {
      val ac = alphanumericPlate.alphanumericColumns

      //                                         layerOffset, frontBackOffset, leftRightOffset, columnOffset
      alphanumericColumns(ac, KeySwitch.BOTTOM_HEIGHT - 1.mm,          1.5.mm,          1.5.mm,         1.mm)
      alphanumericColumns(ac,                           0.mm,         20.0.mm,          3.0.mm,         0.mm)

      for (c in ac.columns) {
         for (k in c.keySwitches) {
            switchHole(k)
         }
      }
   }
}

/**
 * @param layerOffset
 * 各KeyPlateの位置が[KeySwitch.bottomVector]方向へ移動する
 * @param frontBackOffset
 * 各Column手前と奥に広がるが、Ninja60の場合手前と奥のKeyPlateは上を向いているので上に広がる
 * @param leftRightOffset
 * 一番左のColumnの左、一番右のColumnの右が広がる
 * @param columnOffset
 * 各Column 左右方向に広がる
 */
private fun ScadWriter.alphanumericColumns(
   alphanumericColumns: AlphanumericColumns,
   layerOffset: Size,
   frontBackOffset: Size,
   leftRightOffset: Size,
   columnOffset: Size
) {
   fun Plane3d.translateByNormalVector(size: Size): Plane3d {
      return translate(normalVector, size)
   }

   val switches: List<List<KeySwitch>> = alphanumericColumns.columns.map { column ->
      column.keySwitches.map { it.translate(it.bottomVector, layerOffset) }
   }

   val plates: List<List<KeyPlate>> = switches.map { columnSwitches ->
      columnSwitches.map {
         KeyPlate(it.center, AlphanumericPlate.KEY_PLATE_SIZE, -it.bottomVector, it.frontVector)
      }
   }

   val wallPlanes = getWallPlanes(alphanumericColumns.columns, leftRightOffset)

   // --------

   union {
      for ((wallPlane, plate) in wallPlanes.zipWithNext() zip plates) {
         column(plate,
            wallPlane.first .translateByNormalVector(-columnOffset),
            wallPlane.second.translateByNormalVector( columnOffset),
            layerOffset, frontBackOffset)
      }
   }
}

private fun ScadWriter.column(
   columnPlates: List<KeyPlate>,
   leftWallPlane: Plane3d,
   rightWallPlane: Plane3d,
   layerOffset: Size,
   frontBackOffset: Size
) {
   val mostBackPlate  = columnPlates.first()
   val mostFrontPlate = columnPlates.last()

   val boundaryLines = columnBoundaryLines(columnPlates)

   val mostBackLine  = boundaryLines.first().translate(mostBackPlate .frontVector, -frontBackOffset)
   val mostFrontLine = boundaryLines.last() .translate(mostFrontPlate.frontVector,  frontBackOffset)

   val lines = listOf(
      mostBackLine.translate(mostBackPlate.normalVector, layerOffset),
      mostBackLine,
      *boundaryLines.drop(1).dropLast(1).toTypedArray(),
      mostFrontLine,
      mostFrontLine.translate(mostFrontPlate.normalVector, layerOffset)
   )

   hullPoints(
      lines.map { leftWallPlane  intersection it } +
      lines.map { rightWallPlane intersection it }
   )
}

private fun columnBoundaryLines(columnPlates: List<KeyPlate>): List<Line3d> {
   val lines = ArrayList<Line3d>()

   val mostBackPlate = columnPlates.first()
   lines += Line3d(mostBackPlate.backLeft, mostBackPlate.backRight)

   for ((back, front) in columnPlates.zipWithNext()) {
      val backPlane  = Plane3d(back .center, back .normalVector)
      val frontPlane = Plane3d(front.center, front.normalVector)
      lines += backPlane intersection frontPlane
   }

   val mostFrontPlate = columnPlates.last()
   lines += Line3d(mostFrontPlate.frontLeft, mostFrontPlate.frontRight)

   return lines
}

private fun getWallPlanes(columns: List<Column>, leftRightOffset: Size): List<Plane3d> {
   fun Column.rightVector() = frontVector vectorProduct bottomVector

   val planes = ArrayList<Plane3d>()

   planes += run {
      val leftmostColumn = columns.first()

      Plane3d(
            leftmostColumn.referencePoint
               .translate(
                  leftmostColumn.rightVector(),
                  -AlphanumericPlate.KEY_PLATE_SIZE.x / 2
               ),
            leftmostColumn.rightVector()
         )
         .translate(leftmostColumn.rightVector(), -leftRightOffset)
   }

   for ((left, right) in columns.zipWithNext()) {
      planes += getWallPlane(left, right)
   }

   planes += run {
      val rightmostColumn = columns.last()

      Plane3d(
            rightmostColumn.referencePoint
               .translate(rightmostColumn.rightVector(), AlphanumericPlate.KEY_PLATE_SIZE.x / 2),
            rightmostColumn.rightVector()
         )
         .translate(rightmostColumn.rightVector(), leftRightOffset)
   }

   return planes
}

private fun getWallPlane(leftColumn: Column, rightColumn: Column): Plane3d {
   val alignmentVector = run {
      val (lx, ly, lz) = leftColumn .frontVector
      val (rx, ry, rz) = rightColumn.frontVector
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
