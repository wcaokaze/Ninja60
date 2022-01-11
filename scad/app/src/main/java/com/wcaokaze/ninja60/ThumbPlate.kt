package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * 親指で押すキースイッチを挿すためのプレート。
 *
 * @param bottomVector 下向きの方向を表すベクトル。
 * @param frontVector 手前方向を表すベクトル。
 */
data class ThumbPlate(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : Transformable<ThumbPlate> {
   companion object {
      val KEY_PLATE_SIZE = Size2d(17.5.mm, 17.5.mm)
   }

   /**
    * 左右方向に並ぶキーのリスト。左から右の順。
    * 凹面を作ることは問題ないが、一周して円にすることはできないものとする。
    *
    * columnではなくrowでは？ というのは気にしない方針で
    */
   val column: List<KeySwitch> get() {
      val row2 = KeySwitch(
            referencePoint,
            KeySwitch.LayoutSize(1.0, 1.3),
            bottomVector, frontVector
         )
         .translate(bottomVector, Keycap.THICKNESS + KeySwitch.STEM_HEIGHT + KeySwitch.TOP_HEIGHT)

      val row1 = row2
         .let { row1 ->
            row1
               .translate(leftVector, KEY_PLATE_SIZE.x * row2.layoutSize.x / 2)
               .translate(leftVector, KEY_PLATE_SIZE.x * row1.layoutSize.x / 2)
         }
         .rotate(
            Line3d(
               referencePoint.translate(leftVector, KEY_PLATE_SIZE.x * row2.layoutSize.x / 2),
               backVector
            ),
            80.deg
         )

      val row3 = row2
         .let { row3 ->
            row3
               .translate(rightVector, KEY_PLATE_SIZE.x * row2.layoutSize.x / 2)
               .translate(rightVector, KEY_PLATE_SIZE.x * row3.layoutSize.x / 2)
         }
         .rotate(
            Line3d(
               referencePoint.translate(rightVector, KEY_PLATE_SIZE.x * row2.layoutSize.x / 2),
               frontVector
            ),
            80.deg
         )

      return listOf(row1, row2, row3)
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = ThumbPlate(frontVector, bottomVector, referencePoint)
}

// =============================================================================

fun ScadParentObject.thumbPlate(thumbPlate: ThumbPlate): ScadObject {
   return union {
      difference {
         //                                     layerOffset, leftRightOffset, frontOffset
         hullThumbPlate(thumbPlate, KeySwitch.BOTTOM_HEIGHT,          1.5.mm,      1.5.mm)
         hullThumbPlate(thumbPlate,                    0.mm,         20.0.mm,     20.0.mm)

         for (k in thumbPlate.column) {
            switchHole(k)
         }
      }

      for (k in thumbPlate.column) {
         switchSideHolder(k)
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
