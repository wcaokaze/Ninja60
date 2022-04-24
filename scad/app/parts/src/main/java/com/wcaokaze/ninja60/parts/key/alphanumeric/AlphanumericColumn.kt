package com.wcaokaze.ninja60.parts.key.alphanumeric

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.parts.key.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.ninja60.shared.scadutil.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * [KeyPlate] をY軸方向に複数枚並べた列。
 *
 * @param twistAngle
 * [KeySwitch.frontVector]の向きの直線を軸として各KeySwitchを回転する。
 * ただし、軸とする直線の位置は回転結果が[KeySwitch.bottomVector]の反対側に上がるように選択される。
 * 具体的にはtwistAngleが正のときKeySwitchの左端、twistAngleが負のときKeySwitchの右端が
 * 軸となる。
 */
data class AlphanumericColumn(
   override val referencePoint: Point3d,
   override val bottomVector: Vector3d,
   override val frontVector: Vector3d,
   val radius: Size,
   val twistAngle: Angle
) : Transformable<AlphanumericColumn> {
   /** この列に含まれる[KeySwitch]のリスト。上から順 */
   val keySwitches: List<KeySwitch> get() {
      val keycapTop = referencePoint.translate(bottomVector, radius)
      val alignmentAxis = Line3d(referencePoint, rightVector)

      val nonTwistedRow3 = KeySwitch(keycapTop, bottomVector, frontVector)
         .translate(bottomVector, KeySwitch.KEYCAP_SURFACE_HEIGHT)

      val nonTwistedRow2 = nonTwistedRow3
         .let { row2 ->
            row2.rotate(
               alignmentAxis,
               angle = atan(keyPitch.y * nonTwistedRow3.layoutSize.y / 2, radius)
                     + atan(keyPitch.y * row2          .layoutSize.y / 2, radius)
            )
         }

      val nonTwistedRow1 = nonTwistedRow2
         .let { row1 ->
            row1.rotate(
               alignmentAxis,
               angle = atan(keyPitch.y * nonTwistedRow2.layoutSize.y / 2, radius)
                     + atan(keyPitch.y * row1          .layoutSize.y / 2, radius)
            )
         }

      val nonTwistedRow4 = nonTwistedRow3
         .copy(layoutSize = KeySwitch.LayoutSize(1.0, 1.5))
         .let { row4 ->
            row4.translate(
               frontVector,
               distance = keyPitch.y * nonTwistedRow3.layoutSize.y / 2
                        + keyPitch.y * row4          .layoutSize.y / 2
            )
         }
         .rotate(
            Line3d(
               keycapTop.translate(frontVector, keyPitch.y * nonTwistedRow3.layoutSize.y / 2),
               rightVector
            ),
            (-83).deg
         )
         .let { it.translate(it.backVector, KeySwitch.TRAVEL / 2) }

      fun KeySwitch.twist(): KeySwitch {
         val axis = if (twistAngle > 0.deg) {
            Line3d(referencePoint.translate(leftVector,  AlphanumericPlate.KEY_PLATE_SIZE.x), frontVector)
         } else {
            Line3d(referencePoint.translate(rightVector, AlphanumericPlate.KEY_PLATE_SIZE.x), frontVector)
         }

         return rotate(axis, twistAngle)
      }

      return listOf(
         nonTwistedRow1.twist(),
         nonTwistedRow2.twist(),
         nonTwistedRow3.twist(),
         nonTwistedRow4.twist()
      )
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = AlphanumericColumn(referencePoint, bottomVector, frontVector, radius, twistAngle)
}
