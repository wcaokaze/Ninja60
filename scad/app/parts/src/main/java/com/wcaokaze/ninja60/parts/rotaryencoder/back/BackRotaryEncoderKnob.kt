package com.wcaokaze.ninja60.parts.rotaryencoder.back

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.parts.rotaryencoder.gear.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.ninja60.shared.scadutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * 奥側のロータリーエンコーダのノブ。
 * ここのノブは直接ロータリーエンコーダに挿しておらず、
 * [BackRotaryEncoderMediationGear]と[BackRotaryEncoderGear]を経由して
 * ロータリーエンコーダに回転が伝わる。
 */
data class BackRotaryEncoderKnob(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : Transformable<BackRotaryEncoderKnob> {
   companion object {
      val RADIUS = 10.mm
      val HEIGHT = 15.mm
      val SHAFT_HOLE_RADIUS = 1.1.mm
      val GEAR_THICKNESS = 2.mm

      val SKIDPROOF_COUNT = 32
      val SKIDPROOF_RADIUS = 0.25.mm
   }

   val gear: Gear get() {
      val module = BackRotaryEncoderMediationGear.SpurGear.MODULE
      val diameter = RADIUS * 2 - module * 2

      val toothCount = (diameter / module).toInt()

      return Gear(
         BackRotaryEncoderMediationGear.SpurGear.MODULE,
         toothCount,
         GEAR_THICKNESS,
         referencePoint.translate(bottomVector, GEAR_THICKNESS),
         frontVector, bottomVector
      )
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = BackRotaryEncoderKnob(frontVector, bottomVector, referencePoint)
}

fun ScadParentObject.backRotaryEncoderKnob(knob: BackRotaryEncoderKnob): ScadObject {
   fun ScadParentObject.skidproof(): ScadObject {
      val twoPi = Angle.PI * 2
      val skidproofAngle = twoPi / BackRotaryEncoderKnob.SKIDPROOF_COUNT / 2

      return union {
         for (a in 0.0.rad..twoPi step skidproofAngle * 2) {
            arcCylinder(
               BackRotaryEncoderKnob.RADIUS + BackRotaryEncoderKnob.SKIDPROOF_RADIUS,
               BackRotaryEncoderKnob.HEIGHT,
               a - skidproofAngle / 2,
               a + skidproofAngle / 2
            )
         }
      }
   }

   return (
      gear(knob.gear)
      + place(knob) {
         (
            cylinder(BackRotaryEncoderKnob.HEIGHT, BackRotaryEncoderKnob.RADIUS)
            + skidproof()
         )
      }
      - place(knob) {
         cylinder(
            BackRotaryEncoderKnob.HEIGHT * 3,
            BackRotaryEncoderKnob.SHAFT_HOLE_RADIUS,
            center = true
         )
      }
   )
}
