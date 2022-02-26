package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * [FrontRotaryEncoderKnob]の隣にあるキー。
 * [FrontRotaryEncoderKnob]に沿う形で円弧型になっている。
 *
 * [referencePoint]は円弧の中心。
 */
data class FrontRotaryEncoderKey(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : Transformable<FrontRotaryEncoderKey> {
   companion object {
      val KEY_WIDTH = 19.05.mm

      val RADIUS = FrontRotaryEncoderKnob.RADIUS + 0.7.mm + KEY_WIDTH / 2

      operator fun invoke(knob: FrontRotaryEncoderKnob): FrontRotaryEncoderKey {
         return FrontRotaryEncoderKey(
            knob.frontVector .rotate(knob.topVector, 135.deg),
            knob.bottomVector.rotate(knob.topVector, 135.deg),
            knob.referencePoint.translate(knob.bottomVector,
               Keycap.THICKNESS + KeySwitch.STEM_HEIGHT + KeySwitch.TOP_HEIGHT)
         )
      }
   }

   val switch: KeySwitch get() = KeySwitch(
      referencePoint.translate(frontVector, RADIUS),
      KeySwitch.LayoutSize(2.0, 1.0),
      bottomVector, frontVector
   )

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = FrontRotaryEncoderKey(frontVector, bottomVector, referencePoint)
}
