package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * 親指用の[KeyPlate]の集合。
 *
 * [column]と[backKey]に分かれているので注意
 *
 * @param bottomVector 下向きベクトル。
 * @param alignmentVector 手前向きベクトル。
 * @param keySize
 * [column]の各KeyPlateのサイズ。[backKey]には影響しない。
 * また、各KeyPlateの間隔は[keyPitch]で決まっていて、
 * このkeySizeを大きくとったからといってうまく間隔が広がるわけではないことに注意
 */
data class ThumbKeys(
   val referencePoint: Point3d,
   val bottomVector: Vector3d,
   val alignmentVector: Vector3d,
   val radius: Size,
   val keySize: Size2d,
   val layerDistance: Size
) {
   /**
    * 左右方向に並ぶキーのリスト。左から右の順。
    * 凹面を作ることは問題ないが、一周して円にすることはできないものとする。
    *
    * columnではなくrowでは？ というのは気にしない方針で
    */
   val column: List<KeyPlate> get() {
      val alignmentAxis = Line3d(referencePoint, alignmentVector)

      val row2Center = referencePoint.translate(bottomVector, radius)
      val row2 = KeyPlate(
         row2Center, keySize,
         normalVector = -bottomVector,
         frontVector = alignmentVector
      )
      val layeredRow2 = row2.translate(bottomVector, -layerDistance)

      val row1Angle = atan(-keyPitch.x / 2, radius) * 2
      val layeredRow1 = row2
         .translate(bottomVector, -layerDistance)
         .rotate(alignmentAxis, row1Angle)

      val row3Angle = atan(keyPitch.x / 2, radius) * 2
      val layeredRow3 = row2
         .translate(bottomVector, -layerDistance)
         .rotate(alignmentAxis, row3Angle)

      return listOf(layeredRow1, layeredRow2, layeredRow3)
   }

   /**
    * 親指の先、奥にあるキー。
    */
   val backKey: KeyPlate get() {
      val dy = column.maxOf { it.size.y }

      return KeyPlate(
            referencePoint.translate(bottomVector, radius),
            KeyPlate.SIZE,
            normalVector = -bottomVector,
            frontVector = alignmentVector
         )
         .translate(bottomVector, -layerDistance)
         .translate(-alignmentVector, dy / 2 + KeyPlate.SIZE.y / 2)
         .rotate(
            Line3d(referencePoint, alignmentVector vectorProduct bottomVector)
               .translate(bottomVector, radius)
               .translate(-alignmentVector, dy / 2),
            80.deg
         )
   }
}

fun ThumbKeys.translate(distance: Size3d) = ThumbKeys(
   referencePoint.translate(distance),
   bottomVector,
   alignmentVector,
   radius,
   keySize,
   layerDistance
)

fun ThumbKeys.translate(distance: Vector3d): ThumbKeys
      = translate(Size3d(distance.x, distance.y, distance.z))

fun ThumbKeys.translate(direction: Vector3d, distance: Size): ThumbKeys
      = translate(direction.toUnitVector() * distance.numberAsMilliMeter)

fun ThumbKeys.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm
): ThumbKeys = translate(Size3d(x, y, z))

fun ThumbKeys.rotate(axis: Line3d, angle: Angle) = ThumbKeys(
   referencePoint.rotate(axis, angle),
   bottomVector.rotate(axis.vector, angle),
   alignmentVector.rotate(axis.vector, angle),
   radius,
   keySize,
   layerDistance
)
