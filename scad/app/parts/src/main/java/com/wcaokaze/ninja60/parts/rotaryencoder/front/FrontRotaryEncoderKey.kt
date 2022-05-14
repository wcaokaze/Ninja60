package com.wcaokaze.ninja60.parts.rotaryencoder.front

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.parts.key.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.scadwriter.foundation.*
import kotlin.math.*

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
) : TransformableDefaultImpl<FrontRotaryEncoderKey> {
   companion object {
      val KEY_WIDTH = 19.05.mm

      val RADIUS = FrontRotaryEncoderKnob.RADIUS + 0.7.mm + KEY_WIDTH / 2

      private val MIN_ARC_ANGLE = run {
         // 2Uスタビライザーを使うので *直線距離で* 19.05mm×2必要
         val linearDistance = 19.05.mm * 2

         /*                   ____________________________________________________________
          * linearDistance = √ (RADIUS - RADIUS cos ARC_ANGLE)² + (RADIUS sin ARC_ANGLE)²
          *                   _________________________________________________________
          *                = √ RADIUS² - RADIUS² cos²ARC_ANGLE + RADIUS² sin²ARC_ANGLE
          *                   _________________________________________
          *                = √ RADIUS² (-cos²ARC_ANGLE + sin²ARC_ANGLE)
          *                   ________________________________________________
          *                = √ RADIUS² {-(1 - sin²ARC_ANGLE) + sin²ARC_ANGLE}
          *                   _______________________________
          *                = √ RADIUS² (-1 + 2sin²ARC_ANGLE)
          *
          * linearDistance² = RADIUS² (-1 + 2sin²ARC_ANGLE)
          *
          * linearDistance²
          * --------------- = -1 + 2sin²ARC_ANGLE
          *     RADIUS²
          *
          * linearDistance²    1
          * --------------- + --- = sin²ARC_ANGLE
          *    2 RADIUS²       2
          *  _______________________
          * | linearDistance²    1
          * | --------------- + ---  = sin ARC_ANGLE
          * ⎷    2 RADIUS²       2
          *                    _______________________
          *                   | linearDistance²    1
          * ARC_ANGLE = sin⁻¹ | --------------- + ---
          *                   ⎷    2 RADIUS²       2
          */

         Angle(asin(sqrt((linearDistance.numberAsMilliMeter.pow(2.0)
               / RADIUS.numberAsMilliMeter.pow(2.0)) / 2)))
      }

      val ARC_ANGLE = 84.deg.coerceAtLeast(MIN_ARC_ANGLE)
   }

   val switch: KeySwitch get() = KeySwitch(
      referencePoint.translate(frontVector, RADIUS),
      KeySwitch.LayoutSize(2.0, 1.0),
      bottomVector, frontVector
   )

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = FrontRotaryEncoderKey(frontVector, bottomVector, referencePoint)
}
