package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

data class RotaryEncoder(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : Transformable<RotaryEncoder> {
   companion object {
      val HEIGHT = 22.3.mm
      val BODY_SIZE = Size3d(13.4.mm, 22.3.mm, 6.2.mm)
      val SHAFT_HEIGHT = HEIGHT - BODY_SIZE.z
      val SHAFT_RADIUS = 3.4.mm

      /** 基板に取り付ける用の足の高さ */
      val LEG_HEIGHT = 3.2.mm
   }

   init {
      val angle = bottomVector angleWith frontVector

      require(angle >= (90 - 0.01).deg && angle <= (90 + 0.01).deg) {
         "The angle formed by bottomVector and frontVector must be 90 degrees"
      }
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = RotaryEncoder(frontVector, bottomVector, referencePoint)
}

/**
 * [RotaryEncoder]を嵌めるための穴を出力する。
 * EC12Eシリーズ用。固定用の穴、ロータリーエンコーダの端子3つ、押し込みスイッチ用の端子2つ
 *
 * 性質上必ず他のモデルとの[difference]をとることになる。
 */
fun ScadParentObject.rotaryEncoderMountHole(
   rotaryEncoder: RotaryEncoder,
   thickness: Size
): ScadObject {
   fun Point3d.translate(x: Size, y: Size): Point3d {
      return translate(rotaryEncoder.rightVector, x)
            .translate(rotaryEncoder.frontVector, y)
   }

   fun ScadParentObject.hole(positionX: Size, positionY: Size, sizeX: Size, sizeY: Size): ScadObject {
      val holeCenter = rotaryEncoder.referencePoint.translate(positionX, positionY)

      val holePoints = listOf(
         holeCenter.translate(-sizeX / 2.0,  sizeY / 2.0),
         holeCenter.translate( sizeX / 2.0,  sizeY / 2.0),
         holeCenter.translate( sizeX / 2.0, -sizeY / 2.0),
         holeCenter.translate(-sizeX / 2.0, -sizeY / 2.0)
      )

      return hullPoints(
         holePoints.map { it.translate(rotaryEncoder.topVector, 0.1.mm) } +
         holePoints.map { it.translate(rotaryEncoder.bottomVector, thickness) }
      )
   }

   return union {
      hole((-2.5).mm, (-7.5).mm, 1.mm, 1.mm)
      hole(  0.0 .mm, (-7.5).mm, 1.mm, 1.mm)
      hole(  2.5 .mm, (-7.5).mm, 1.mm, 1.mm)

      hole((-5.6).mm, 0.mm, 2.mm, 2.5.mm)
      hole(  5.6 .mm, 0.mm, 2.mm, 2.5.mm)

      hole((-2.5).mm, 7.mm, 1.mm, 1.mm)
      hole(  2.5 .mm, 7.mm, 1.mm, 1.mm)
   }
}

/**
 * ロータリーエンコーダのノブをシャフトに挿すとき用の穴。
 */
fun ScadParentObject.rotaryEncoderKnobHole(
   rotaryEncoder: RotaryEncoder
): ScadObject {
   return locale(rotaryEncoder.referencePoint) {
      rotate(
         -Vector3d.Z_UNIT_VECTOR angleWith rotaryEncoder.bottomVector,
         -Vector3d.Z_UNIT_VECTOR vectorProduct rotaryEncoder.bottomVector,
      ) {
         union {
            difference {
               cylinder(RotaryEncoder.HEIGHT + 0.1.mm, 3.mm, `$fa`)

               translate(x = 1.45.mm, y = (-3).mm) {
                  cube(3.mm, 6.mm, RotaryEncoder.HEIGHT + 0.1.mm)
               }
            }

            cylinder(RotaryEncoder.HEIGHT - 3.mm, RotaryEncoder.SHAFT_RADIUS, `$fa`)
         }
      }
   }
}
