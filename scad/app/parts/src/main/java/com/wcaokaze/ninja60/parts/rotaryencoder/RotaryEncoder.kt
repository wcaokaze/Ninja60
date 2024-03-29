package com.wcaokaze.ninja60.parts.rotaryencoder

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.ninja60.shared.scadutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

data class RotaryEncoder(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : TransformableDefaultImpl<RotaryEncoder> {
   companion object {
      val HEIGHT = 22.3.mm
      val BODY_SIZE = Size3d(13.4.mm, 16.3.mm, 6.2.mm)
      val SHAFT_HEIGHT = HEIGHT - BODY_SIZE.z
      val SHAFT_RADIUS = 3.5.mm

      /** 基板に取り付ける用の足の高さ */
      val LEG_HEIGHT = 3.2.mm

      /** クリックするときの押し込みの深さ */
      val CLICK_TRAVEL = 1.5.mm

      /** ロータリーエンコーダを取り付ける基板の厚み */
      val BOARD_THICKNESS = 1.6.mm
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
   fun ScadParentObject.hole(positionX: Size, positionY: Size, sizeX: Size, sizeY: Size): ScadObject {
      return translate(positionX - sizeX / 2, positionY - sizeY / 2) {
         cube(sizeX, sizeY, thickness + 0.2.mm)
      }
   }

   return place(rotaryEncoder) {
      translate(z = -thickness - 0.1.mm) {
         union {
            hole((-2.5).mm, (-7.5).mm, 1.mm, 1.mm)
            hole(  0.0 .mm, (-7.5).mm, 1.mm, 1.mm)
            hole(  2.5 .mm, (-7.5).mm, 1.mm, 1.mm)

            hole((-5.6).mm, 0.mm, 2.mm, 2.5.mm)
            hole(  5.6 .mm, 0.mm, 2.mm, 2.5.mm)

            hole((-2.5).mm, 7.mm, 1.mm, 1.mm)
            hole(  2.5 .mm, 7.mm, 1.mm, 1.mm)
         }
      }
   }
}

/**
 * ロータリーエンコーダのノブをシャフトに挿すとき用の穴。
 */
fun ScadParentObject.rotaryEncoderKnobHole(
   rotaryEncoder: RotaryEncoder
): ScadObject {
   return place(rotaryEncoder) {
      union {
         difference {
            cylinder(RotaryEncoder.HEIGHT + 0.1.mm, 3.mm)

            translate(x = 1.45.mm, y = (-3).mm) {
               cube(3.mm, 6.mm, RotaryEncoder.HEIGHT + 0.1.mm)
            }
         }

         cylinder(RotaryEncoder.HEIGHT - 3.mm, RotaryEncoder.SHAFT_RADIUS + 0.1.mm)
      }
   }
}
