package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * [KeyPlate] をY軸方向に複数枚並べた列。
 *
 * @param bottomVector
 * 下向きの方向を表すベクトル。全く回転していないColumnの場合Z軸の負の方向。
 *
 * @param frontVector
 * 手前方向を表すベクトル。全く回転していないColumnの場合Y軸の負の方向。
 *
 * @param twistAngle
 * [KeySwitch.frontVector]の向きの直線を軸として各KeySwitchを回転する。
 * ただし、軸とする直線の位置は回転結果が[KeySwitch.bottomVector]の反対側に上がるように選択される。
 * 具体的にはtwistAngleが正のときKeySwitchの左端、twistAngleが負のときKeySwitchの右端が
 * 軸となる。
 */
data class Column(
   val referencePoint: Point3d,
   val bottomVector: Vector3d,
   val frontVector: Vector3d,
   val radius: Size,
   val twistAngle: Angle
) {
   /** この列に含まれる[KeySwitch]のリスト。上から順 */
   val keySwitches: List<KeySwitch> get() {
      val keycapTop = referencePoint.translate(bottomVector, radius)
      val rightVector = frontVector vectorProduct bottomVector
      val alignmentAxis = Line3d(referencePoint, rightVector)

      val nonTwistedRow3 = KeySwitch(keycapTop, bottomVector, frontVector)
         .translate(bottomVector, Keycap.THICKNESS + KeySwitch.STEM_HEIGHT + KeySwitch.TOP_HEIGHT)

      val nonTwistedRow2 = nonTwistedRow3.rotate(alignmentAxis, atan(keyPitch.y / 2, radius) * 2)
      val nonTwistedRow1 = nonTwistedRow2.rotate(alignmentAxis, atan(keyPitch.y / 2, radius) * 2)

      val nonTwistedRow4 = nonTwistedRow3
         .translate(frontVector, keyPitch.y)
         .rotate(
            Line3d(
               keycapTop.translate(frontVector, keyPitch.y / 2),
               rightVector
            ),
            (-83).deg
         )

      fun KeySwitch.twist(): KeySwitch {
         val axis = if (twistAngle > 0.deg) {
            Line3d(center.translate(rightVector, -AlphanumericPlate.KEY_PLATE_SIZE.x), frontVector)
         } else {
            Line3d(center.translate(rightVector,  AlphanumericPlate.KEY_PLATE_SIZE.x), frontVector)
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
}

fun Column.translate(distance: Size3d) = Column(
   referencePoint.translate(distance),
   bottomVector,
   frontVector,
   radius,
   twistAngle
)

fun Column.translate(distance: Vector3d): Column
      = translate(Size3d(distance.x, distance.y, distance.z))

fun Column.translate(direction: Vector3d, distance: Size): Column
      = translate(direction.toUnitVector() * distance.numberAsMilliMeter)

fun Column.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm
): Column = translate(Size3d(x, y, z))

fun Column.rotate(axis: Line3d, angle: Angle) = Column(
   referencePoint.rotate(axis, angle),
   bottomVector.rotate(axis.vector, angle),
   frontVector.rotate(axis.vector, angle),
   radius,
   twistAngle
)
