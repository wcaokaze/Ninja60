package com.wcaokaze.ninja60.parts.key.alphanumeric

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.parts.key.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.ninja60.shared.scadutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * 人差し指から小指の4本の指で押す、アルファベットと数字のキースイッチを挿すためのプレート
 */
data class AlphanumericPlate(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : TransformableDefaultImpl<AlphanumericPlate> {
   companion object {
      val KEY_PLATE_SIZE = Size2d(17.5.mm, 17.5.mm)
   }

   /** 小指側から人差し指側の順 */
   val columns: List<AlphanumericColumn> get() {
      fun column(
         dx: Size, dy: Size, dz: Size,
         radius: Size,
         az: Angle, ax: Angle,
         twist: Angle
      ): AlphanumericColumn {
         return AlphanumericColumn(
               referencePoint.translate(backVector, dy),
               bottomVector,
               frontVector,
               radius,
               twist
            )
            .rotate(
               Line3d(referencePoint, topVector).translate(frontVector, 30.mm),
               az
            )
            .let { column ->
               column.rotate(
                  Line3d(
                     column.referencePoint
                        .translate(column.bottomVector, radius),
                     column.frontVector
                  ),
                  ax
               )
            }
            .translate(rightVector, dx)
            .translate(topVector, dz)
      }

      return listOf(
         //    | dx                    | dy      | dz     | radius | az      | ax        | twist   |
         column(keyPitch.x * -2 - 19.mm, (-18).mm,  9.5.mm,   38.mm,   4 .deg,   1.5 .deg, (-8).deg),
         column(keyPitch.x * -2        , (-16).mm, 10.0.mm,   38.mm,   4 .deg,   3.0 .deg,   0 .deg),
         column(keyPitch.x * -1        , (- 5).mm,  5.0.mm,   42.mm,   2 .deg,   2.0 .deg,   0 .deg),
         column(keyPitch.x *  0        ,    0 .mm,  0.0.mm,   44.mm,   0 .deg,   0.0 .deg,   0 .deg),
         column(keyPitch.x *  1        , (- 3).mm,  4.0.mm,   41.mm, (-2).deg, (-1.0).deg,   0 .deg),
         column(keyPitch.x *  1 + 19.mm, (- 5).mm,  4.1.mm,   41.mm, (-2).deg,   0.5 .deg,   8 .deg),
      )
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = AlphanumericPlate(frontVector, bottomVector, referencePoint)
}

// =============================================================================

data class HullAlphanumericConfig(
   /** 各KeyPlateの位置が[KeySwitch.bottomVector]方向へ移動する */
   val layerOffset: Size = 0.mm,
   /** 各Column手前と奥に広がる */
   val frontBackOffset: Size = 0.mm,
   /** 一番左のColumnの左、一番右のColumnの右が広がる */
   val leftRightOffset: Size = 0.mm,
   /** 各Column 左右方向に広がる */
   val columnOffset: Size = 0.mm
)

fun ScadParentObject.hullAlphanumericPlate(
   alphanumericPlate: AlphanumericPlate,
   config: HullAlphanumericConfig
): ScadObject {
   return union {
      val columns = listOf(null, *alphanumericPlate.columns.toTypedArray(), null)

      for ((left, current, right) in columns.windowed(3)) {
         hullColumn(current!!, left, right, config)
      }
   }
}

fun ScadParentObject.hullColumn(
   column: AlphanumericColumn,
   leftColumn: AlphanumericColumn?,
   rightColumn: AlphanumericColumn?,
   config: HullAlphanumericConfig
): ScadObject {
   val lines = run {
      val columnPlates = column.keySwitches
         .map { it.translate(it.bottomVector, config.layerOffset) }
         .map { it.plate(AlphanumericPlate.KEY_PLATE_SIZE) }

      val mostBackPlate  = columnPlates.first()
      val mostFrontPlate = columnPlates.last()

      val boundaryLines = columnBoundaryLines(columnPlates)

      val mostBackLine  = boundaryLines.first().translate(mostBackPlate.backVector,   config.frontBackOffset)
      val mostFrontLine = boundaryLines.last() .translate(mostFrontPlate.frontVector, config.frontBackOffset)

      listOf(
         mostBackLine.translate(mostBackPlate.topVector, config.layerOffset.coerceAtLeast(20.mm)),
         mostBackLine,
         *boundaryLines.drop(1).dropLast(1).toTypedArray(),
         mostFrontLine,
         mostFrontLine.translate(mostFrontPlate.topVector, config.layerOffset.coerceAtLeast(20.mm))
      )
   }

   val (leftWallPlane, rightWallPlane) = getColumnWallPlane(column, leftColumn, rightColumn, config)

   return hullPoints(
      lines.map { leftWallPlane  intersection it } +
      lines.map { rightWallPlane intersection it }
   )
}

private fun columnBoundaryLines(columnPlates: List<KeyPlate>): List<Line3d> {
   val lines = ArrayList<Line3d>()

   val mostBackPlate = columnPlates.first()
   lines += Line3d(mostBackPlate.backLeft, mostBackPlate.backRight)

   for ((back, front) in columnPlates.zipWithNext()) {
      val backPlane  = Plane3d(back .referencePoint, back .bottomVector)
      val frontPlane = Plane3d(front.referencePoint, front.bottomVector)
      lines += backPlane intersection frontPlane
   }

   val mostFrontPlate = columnPlates.last()
   lines += Line3d(mostFrontPlate.frontLeft, mostFrontPlate.frontRight)

   return lines
}

private fun getLeftPlane(column: AlphanumericColumn): Plane3d {
   return Plane3d(
      column.referencePoint
         .translate(
            column.leftVector,
            AlphanumericPlate.KEY_PLATE_SIZE.x * column.keySwitches.maxOf { it.layoutSize.x } / 2
         ),
      column.rightVector
   )
}

private fun getRightPlane(column: AlphanumericColumn): Plane3d {
   return Plane3d(
      column.referencePoint
         .translate(
            column.rightVector,
            AlphanumericPlate.KEY_PLATE_SIZE.x * column.keySwitches.maxOf { it.layoutSize.x } / 2
         ),
      column.rightVector
   )
}

val AlphanumericPlate.leftmostPlane: Plane3d
   get() = getLeftPlane(columns.first())

val AlphanumericPlate.rightmostPlane: Plane3d
   get() = getRightPlane(columns.last())

private fun getColumnWallPlane(
   column: AlphanumericColumn,
   left: AlphanumericColumn?,
   right: AlphanumericColumn?,
   config: HullAlphanumericConfig
): Pair<Plane3d, Plane3d> {
   val leftWallPlane = if (left == null) {
      getLeftPlane(column).translate(column.leftVector, config.leftRightOffset)
   } else {
      getWallPlane(left, column)
   }

   val rightWallPlane = if (right == null) {
      getRightPlane(column).translate(column.rightVector, config.leftRightOffset)
   } else {
      getWallPlane(column, right)
   }

   return Pair(leftWallPlane, rightWallPlane)
}

fun getWallPlane(leftColumn: AlphanumericColumn, rightColumn: AlphanumericColumn): Plane3d {
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
