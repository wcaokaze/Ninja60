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
 */
data class ThumbKeys(
   val referencePoint: Point3d,
   val bottomVector: Vector3d,
   val alignmentVector: Vector3d,
   val radius: Size,
   val layerDistance: Size
) {
   private fun Vector3d.norm(norm: Size): Vector3d = toUnitVector() * norm.numberAsMilliMeter

   /**
    * 左右方向に並ぶキーのリスト。左から右の順。
    * 凹面を作ることは問題ないが、一周して円にすることはできないものとする。
    *
    * columnではなくrowでは？ というのは気にしない方針で
    */
   val column: List<KeyPlate> get() {
      val alignmentAxis = Line3d(referencePoint, alignmentVector)

      val row2Center = referencePoint.translate(bottomVector.norm(radius))
      val row2 = KeyPlate(
         row2Center, KeyPlate.SIZE,
         normalVector = -bottomVector,
         frontVector = alignmentVector
      )
      val layeredRow2 = row2.translate(bottomVector.norm(-layerDistance))

      val row1Angle = atan(-keyPitch.x / 2, radius) * 2
      val layeredRow1 = row2
         .translate(bottomVector.norm(-layerDistance))
         .rotate(alignmentAxis, row1Angle)

      val row3Angle = atan(keyPitch.x / 2, radius) * 2
      val layeredRow3 = row2
         .translate(bottomVector.norm(-layerDistance))
         .rotate(alignmentAxis, row3Angle)

      return listOf(layeredRow1, layeredRow2, layeredRow3)
   }

   /**
    * 親指の先、奥にあるキー。
    */
   val backKey: KeyPlate get() {
      return KeyPlate(
            referencePoint.translate(bottomVector.norm(radius)),
            KeyPlate.SIZE,
            normalVector = -bottomVector,
            frontVector = alignmentVector
         )
         .translate(bottomVector.norm(-layerDistance))
         .translate(alignmentVector.norm(-keyPitch.y))
         .rotate(
            Line3d(referencePoint, alignmentVector vectorProduct bottomVector)
               .translate(bottomVector.norm(radius))
               .translate(alignmentVector.norm(-keyPitch.y / 2)),
            80.deg
         )
   }
}
