package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * 親指で押すキースイッチを挿すためのプレート。
 *
 * このプレートの[KeySwitch]を扱う場合[column]と[backKey]に分かれているので注意
 *
 * @param bottomVector 下向きの方向を表すベクトル。
 * @param frontVector 手前方向を表すベクトル。
 */
data class ThumbPlate(
   override val referencePoint: Point3d,
   override val bottomVector: Vector3d,
   override val frontVector: Vector3d,
   val radius: Size
) : Transformable<ThumbPlate> {
   companion object {
      val KEY_PLATE_SIZE = Size2d(17.5.mm, 17.5.mm)

      operator fun invoke() = ThumbPlate(
         referencePoint = Point3d.ORIGIN,
         bottomVector = -Vector3d.Z_UNIT_VECTOR,
         frontVector = -Vector3d.Y_UNIT_VECTOR,
         radius = 16.mm
      )
   }

   /**
    * 左右方向に並ぶキーのリスト。左から右の順。
    * 凹面を作ることは問題ないが、一周して円にすることはできないものとする。
    *
    * columnではなくrowでは？ というのは気にしない方針で
    */
   val column: List<KeySwitch> get() {
      val alignmentAxis = Line3d(referencePoint, frontVector)

      val row2 = KeySwitch(
            referencePoint.translate(bottomVector, radius),
            KeySwitch.LayoutSize(1.0, 1.3),
            bottomVector, frontVector
         )
         .translate(bottomVector, Keycap.THICKNESS + KeySwitch.STEM_HEIGHT + KeySwitch.TOP_HEIGHT)

      val row1 = row2
         .let { row1 ->
            row1.rotate(
               alignmentAxis,
               angle = atan(keyPitch.x * -row2.layoutSize.x / 2, radius)
                     + atan(keyPitch.x * -row1.layoutSize.x / 2, radius)
            )
         }

      val row3 = row2
         .let { row3 ->
            row3.rotate(
               alignmentAxis,
               angle = atan(keyPitch.x * row2.layoutSize.x / 2, radius)
                     + atan(keyPitch.x * row3.layoutSize.x / 2, radius)
            )
         }

      return listOf(row1, row2, row3)
   }

   /**
    * 親指の先、奥にあるキー。
    */
   val backKey: KeySwitch get() {
      val columnCenterKey = column[column.size / 2]

      return columnCenterKey
         .copy(layoutSize = KeySwitch.LayoutSize(1.0, 1.0))
         .translate(bottomVector, Keycap.THICKNESS + KeySwitch.STEM_HEIGHT + KeySwitch.TOP_HEIGHT)
         .let { backKey ->
            backKey.translate(
               -frontVector,
               distance = keyPitch.y * columnCenterKey.layoutSize.y / 2
                        + keyPitch.y * backKey        .layoutSize.y / 2
            )
         }
         .rotate(
            Line3d(referencePoint, frontVector vectorProduct bottomVector)
               .translate(bottomVector, radius)
               .translate(-frontVector, keyPitch.y * columnCenterKey.layoutSize.y / 2),
            80.deg
         )
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = ThumbPlate(referencePoint, bottomVector, frontVector, radius)
}

// =============================================================================

fun ScadWriter.thumbPlate(thumbPlate: ThumbPlate) {
   union {
      difference {
         //                                     layerOffset, leftRightOffset, frontOffset
         hullThumbPlate(thumbPlate, KeySwitch.BOTTOM_HEIGHT,          1.5.mm,      1.5.mm)
         hullThumbPlate(thumbPlate,                    0.mm,         20.0.mm,     20.0.mm)

         for (k in thumbPlate.column + thumbPlate.backKey) {
            switchHole(k)
         }
      }

      for (k in thumbPlate.column + thumbPlate.backKey) {
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
fun ScadWriter.hullThumbPlate(
   thumbPlate: ThumbPlate,
   layerOffset: Size = 0.mm,
   leftRightOffset: Size = 0.mm,
   frontOffset: Size = 0.mm
) {
   fun Plane3d.translateByNormalVector(size: Size): Plane3d {
      return translate(normalVector, size)
   }

   val columnSwitches = thumbPlate.column.map { it.translate(it.bottomVector, layerOffset) }
   val columnPlates = columnSwitches.map { it.plate(ThumbPlate.KEY_PLATE_SIZE) }
   val backKeySwitch = thumbPlate.backKey.translate(thumbPlate.backKey.bottomVector, layerOffset)
   val backKeyPlate = backKeySwitch.plate(ThumbPlate.KEY_PLATE_SIZE)

   val frontWallPlane = Plane3d(
         thumbPlate.referencePoint
            .translate(
               thumbPlate.frontVector,
               ThumbPlate.KEY_PLATE_SIZE.y * columnSwitches.maxOf { it.layoutSize.y } / 2
            ),
         thumbPlate.frontVector
      )
      .translateByNormalVector(frontOffset)

   val backWallPlane = Plane3d(backKeySwitch.referencePoint, -backKeySwitch.bottomVector)

   val leftmostPlate  = columnPlates.first()
   val rightmostPlate = columnPlates.last()

   val boundaryLines = columnBoundaryLines(columnPlates)

   fun KeyPlate.rightVector() = frontVector vectorProduct bottomVector
   val leftmostLine  = boundaryLines.first().translate(leftmostPlate .rightVector(), -leftRightOffset)
   val rightmostLine = boundaryLines.last() .translate(rightmostPlate.rightVector(),  leftRightOffset)

   val columnPoints = listOf(
         leftmostLine.translate(leftmostPlate.bottomVector, -layerOffset),
         leftmostLine,
         *boundaryLines.drop(1).dropLast(1).toTypedArray(),
         rightmostLine,
         rightmostLine.translate(rightmostPlate.bottomVector, -layerOffset)
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
            point.translate(backKeyPlate.bottomVector, -layerOffset),
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
      val leftPlane  = Plane3d(left .referencePoint, left .bottomVector)
      val rightPlane = Plane3d(right.referencePoint, right.bottomVector)
      lines += leftPlane intersection rightPlane
   }

   val rightmostPlate = columnPlates.last()
   lines += Line3d(rightmostPlate.backRight, rightmostPlate.frontRight)

   return lines
}
