package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * 親指用の[KeyPlate]の集合。
 *
 * [column]と[backKey]に分かれているので注意
 *
 * @param bottomVector 下向きの方向を表すベクトル。
 * @param frontVector 手前方向を表すベクトル。
 */
data class ThumbKeys(
   val referencePoint: Point3d,
   val bottomVector: Vector3d,
   val frontVector: Vector3d,
   val radius: Size
) {
   /**
    * 左右方向に並ぶキーのリスト。左から右の順。
    * 凹面を作ることは問題ないが、一周して円にすることはできないものとする。
    *
    * columnではなくrowでは？ というのは気にしない方針で
    */
   val column: List<KeySwitch> get() {
      val alignmentAxis = Line3d(referencePoint, frontVector)

      val row2 = KeySwitch(
            center = referencePoint.translate(bottomVector, radius),
            bottomVector, frontVector
         )
         .translate(bottomVector, Keycap.THICKNESS + KeySwitch.STEM_HEIGHT + KeySwitch.TOP_HEIGHT)

      val row1 = row2
         .rotate(alignmentAxis, atan(-keyPitch.x / 2, radius) * 2)

      val row3 = row2
         .rotate(alignmentAxis, atan(keyPitch.x / 2, radius) * 2)

      return listOf(row1, row2, row3)
   }

   /**
    * 親指の先、奥にあるキー。
    */
   val backKey: KeySwitch get() {
      return KeySwitch(
            center = referencePoint.translate(bottomVector, radius),
            bottomVector, frontVector
         )
         .translate(bottomVector, Keycap.THICKNESS + KeySwitch.STEM_HEIGHT + KeySwitch.TOP_HEIGHT)
         .translate(-frontVector, ThumbPlate.COLUMN_KEY_PLATE_SIZE.y / 2 + ThumbPlate.BACK_KEY_PLATE_SIZE.y / 2)
         .rotate(
            Line3d(referencePoint, frontVector vectorProduct bottomVector)
               .translate(bottomVector, radius)
               .translate(-frontVector, ThumbPlate.COLUMN_KEY_PLATE_SIZE.y / 2),
            80.deg
         )
   }
}

fun ThumbKeys.translate(distance: Size3d) = ThumbKeys(
   referencePoint.translate(distance),
   bottomVector,
   frontVector,
   radius
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
   frontVector.rotate(axis.vector, angle),
   radius
)
