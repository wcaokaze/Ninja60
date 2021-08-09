package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

data class KeySwitch(
   val center: Point3d,

   /** このスイッチの下向きの方向を表すベクトル。 */
   val bottomVector: Vector3d,
   /** このスイッチの手前方向を表すベクトル。 */
   val frontVector: Vector3d
) {
   companion object {
      val TRAVEL = 4.mm

      /** 押し込んでいない状態でのステムの先からスイッチの上面までの距離 */
      val STEM_HEIGHT = 4.mm

      /** (ステムを除いた)キースイッチの高さ */
      val HEIGHT = 11.1.mm

      /** トッププレートの表面からスイッチの底面(足の先ではない)までの距離 */
      val BOTTOM_HEIGHT = 5.mm

      /** スイッチの上面(ステムの先ではない)からトッププレートまでの距離 */
      val TOP_HEIGHT = HEIGHT - BOTTOM_HEIGHT
   }
}

fun KeySwitch.translate(distance: Size3d)
      = KeySwitch(center.translate(distance), bottomVector, frontVector)

fun KeySwitch.translate(distance: Vector3d)
      = KeySwitch(center.translate(distance), bottomVector, frontVector)

fun KeySwitch.translate(direction: Vector3d, distance: Size)
      = KeySwitch(center.translate(direction, distance), bottomVector, frontVector)

fun KeySwitch.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm
): KeySwitch = translate(Size3d(x, y, z))

fun KeySwitch.rotate(axis: Line3d, angle: Angle) = KeySwitch(
   center.rotate(axis, angle),
   bottomVector.rotate(axis.vector, angle),
   frontVector.rotate(axis.vector, angle)
)

private fun KeySwitch.keyPlate(size: Size2d)
      = KeyPlate(center, size, -bottomVector, frontVector)

fun ScadWriter.switchHole(keySwitch: KeySwitch) {
   /* ボトムハウジングの突起部分にハメる穴。本来1.5mmのプレートを使うところ */
   val mountPlateHole = keySwitch.keyPlate(Size2d(14.mm, 14.mm))

   /* スイッチが入る穴。 */
   val switchHole = keySwitch.keyPlate(Size2d(16.mm, 16.mm))

   union {
      hullPoints(
         mountPlateHole.translate(keySwitch.bottomVector, -(0.5).mm).points +
         mountPlateHole.translate(keySwitch.bottomVector,   2.0 .mm).points
      )

      hullPoints(
         switchHole.translate(keySwitch.bottomVector,                  1.5.mm).points +
         switchHole.translate(keySwitch.bottomVector, KeySwitch.BOTTOM_HEIGHT).points
      )
   }
}
