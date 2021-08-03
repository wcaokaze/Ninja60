package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * ロータリーエンコーダを嵌めるための穴。
 *
 * EC12Eシリーズ用。固定用の穴、ロータリーエンコーダの端子3つ、押し込みスイッチ用の端子2つ
 */
data class RotaryEncoderMountHole(
   val center: Point3d,
   val normalVector: Vector3d,
   val frontVector: Vector3d
) {
   init {
      val angle = normalVector angleWith frontVector

      require(angle >= (90 - 0.01).deg && angle <= (90 + 0.01).deg) {
         "The angle formed by normalVector and frontVector must be 90 degrees"
      }
   }
}

fun RotaryEncoderMountHole.translate(distance: Size3d)
      = RotaryEncoderMountHole(center.translate(distance), normalVector, frontVector)

fun RotaryEncoderMountHole.translate(distance: Vector3d)
      = RotaryEncoderMountHole(center.translate(distance), normalVector, frontVector)

fun RotaryEncoderMountHole.translate(direction: Vector3d, distance: Size)
      = RotaryEncoderMountHole(center.translate(direction, distance), normalVector, frontVector)

fun RotaryEncoderMountHole.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm
): RotaryEncoderMountHole = translate(Size3d(x, y, z))

fun RotaryEncoderMountHole.rotate(axis: Line3d, angle: Angle) = RotaryEncoderMountHole(
   center.rotate(axis, angle),
   normalVector.rotate(axis.vector, angle),
   frontVector.rotate(axis.vector, angle)
)

/**
 * [RotaryEncoderMountHole]を出力する。
 *
 * 性質上必ず他のモデルとの[difference]をとることになる。
 *
 * @param zRange
 * 厚み。[RotaryEncoderMountHole.center]を0、
 * [RotaryEncoderMountHole.normalVector]向きを正として、
 * どこからどこまで生成するか。
 */
fun ScadWriter.rotaryEncoderMountHole(
   rotaryEncoderMountHole: RotaryEncoderMountHole,
   zRange: SizeRange
) {
   val rightVector = rotaryEncoderMountHole.normalVector vectorProduct rotaryEncoderMountHole.frontVector

   fun Point3d.translate(x: Size, y: Size): Point3d
      = translate(rightVector, x).translate(rotaryEncoderMountHole.frontVector, y)

   fun ScadWriter.hole(positionX: Size, positionY: Size, sizeX: Size, sizeY: Size) {
      val holeCenter = rotaryEncoderMountHole.center.translate(positionX, positionY)

      val holePoints = listOf(
         holeCenter.translate(-sizeX / 2.0,  sizeY / 2.0),
         holeCenter.translate( sizeX / 2.0,  sizeY / 2.0),
         holeCenter.translate( sizeX / 2.0, -sizeY / 2.0),
         holeCenter.translate(-sizeX / 2.0, -sizeY / 2.0)
      )

      hullPoints(
         holePoints.map { it.translate(rotaryEncoderMountHole.normalVector, zRange.start) } +
         holePoints.map { it.translate(rotaryEncoderMountHole.normalVector, zRange.endInclusive) }
      )
   }

   hole((-2.5).mm, (-7.5).mm, 1.mm, 1.mm)
   hole(  0.0 .mm, (-7.5).mm, 1.mm, 1.mm)
   hole(  2.5 .mm, (-7.5).mm, 1.mm, 1.mm)

   hole((-5.6).mm, 0.mm, 2.mm, 2.5.mm)
   hole(  5.6 .mm, 0.mm, 2.mm, 2.5.mm)

   hole((-2.5).mm, 7.mm, 1.mm, 1.mm)
   hole(  2.5 .mm, 7.mm, 1.mm, 1.mm)
}
