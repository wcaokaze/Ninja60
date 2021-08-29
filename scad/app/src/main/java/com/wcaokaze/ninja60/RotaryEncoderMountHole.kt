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
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : Transformable<RotaryEncoderMountHole> {
   init {
      val angle = bottomVector angleWith frontVector

      require(angle >= (90 - 0.01).deg && angle <= (90 + 0.01).deg) {
         "The angle formed by normalVector and frontVector must be 90 degrees"
      }
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = RotaryEncoderMountHole(frontVector, bottomVector, referencePoint)
}

/**
 * [RotaryEncoderMountHole]を出力する。
 *
 * 性質上必ず他のモデルとの[difference]をとることになる。
 *
 * @param zRange
 * 厚み。[RotaryEncoderMountHole.referencePoint]を0、
 * [RotaryEncoderMountHole.topVector]向きを正として、
 * どこからどこまで生成するか。
 */
fun ScadWriter.rotaryEncoderMountHole(
   rotaryEncoderMountHole: RotaryEncoderMountHole,
   zRange: SizeRange
) {
   fun Point3d.translate(x: Size, y: Size): Point3d {
      return translate(rotaryEncoderMountHole.rightVector, x)
            .translate(rotaryEncoderMountHole.frontVector, y)
   }

   fun ScadWriter.hole(positionX: Size, positionY: Size, sizeX: Size, sizeY: Size) {
      val holeCenter = rotaryEncoderMountHole.referencePoint.translate(positionX, positionY)

      val holePoints = listOf(
         holeCenter.translate(-sizeX / 2.0,  sizeY / 2.0),
         holeCenter.translate( sizeX / 2.0,  sizeY / 2.0),
         holeCenter.translate( sizeX / 2.0, -sizeY / 2.0),
         holeCenter.translate(-sizeX / 2.0, -sizeY / 2.0)
      )

      hullPoints(
         holePoints.map { it.translate(rotaryEncoderMountHole.topVector, zRange.start) } +
         holePoints.map { it.translate(rotaryEncoderMountHole.topVector, zRange.endInclusive) }
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
