package com.wcaokaze.ninja60.parts.rotaryencoder.back

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.parts.rotaryencoder.*
import com.wcaokaze.ninja60.parts.rotaryencoder.gear.*
import com.wcaokaze.ninja60.parts.rotaryencoder.gear.BevelGear as Bevel
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

// =============================================================================

data class BackRotaryEncoderMediationGear(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : Transformable<BackRotaryEncoderMediationGear> {
   object SpurGear {
      val MODULE = 1.5.mm
      val TOOTH_COUNT = 16
      val THICKNESS = 2.mm
   }

   object BevelGear {
      val MODULE = 1.5.mm
      val OPERATING_ANGLE = 90.deg
      val TOOTH_COUNT = 10
      val THICKNESS = 4.mm

      fun createPair(): Pair<Bevel, Bevel> {
         return Bevel.createPair(
            MODULE,
            OPERATING_ANGLE,
            TOOTH_COUNT,
            BackRotaryEncoderGear.Gear.TOOTH_COUNT,
            THICKNESS
         )
      }
   }

   companion object {
      val SHAFT_HOLE_RADIUS = 1.1.mm
   }

   val spurGear get() = Gear(
      SpurGear.MODULE,
      SpurGear.TOOTH_COUNT,
      SpurGear.THICKNESS,
      referencePoint,
      frontVector,
      bottomVector
   )

   val bevelGear: Bevel get() {
      val (bevelGear, _) = BevelGear.createPair()
      return bevelGear.copy(
         referencePoint.translate(topVector, SpurGear.THICKNESS),
         frontVector,
         bottomVector
      )
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = BackRotaryEncoderMediationGear(frontVector, bottomVector, referencePoint)
}

fun ScadParentObject.backRotaryEncoderMediationGear(gear: BackRotaryEncoderMediationGear): ScadObject {
   return (
      gear(gear.spurGear)
      + bevelGear(gear.bevelGear)
      - place(gear) {
         cylinder(
            BackRotaryEncoderKnob.HEIGHT * 3,
            BackRotaryEncoderMediationGear.SHAFT_HOLE_RADIUS,
            center = true
         )
      }
   )
}

// =============================================================================

/**
 * ロータリーエンコーダに挿すシャフト部分に1枚[Bevel]がついた形状。
 */
data class BackRotaryEncoderGear(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : Transformable<BackRotaryEncoderGear> {
   object Gear {
      val TOOTH_COUNT = 14
   }

   object Shaft {
      val HEIGHT = RotaryEncoder.SHAFT_HEIGHT - 2.mm
      val HOLE_HEIGHT = HEIGHT
      val RADIUS = RotaryEncoder.SHAFT_RADIUS + 1.5.mm

      /**
       * 歯車の位置。[referencePoint]から[topVector]方向の距離
       */
      val GEAR_POSITION = 0.mm

      /** ロータリーエンコーダを入れる際にケースに必要な穴の高さ */
      val INSERTION_HEIGHT =
         RotaryEncoder.LEG_HEIGHT + RotaryEncoder.HEIGHT + 0.5.mm

      /**
       * ロータリーエンコーダを入れる部分のケースの幅
       *
       * ロータリーエンコーダが横向きに設置されるので、
       * ケースの幅はロータリーエンコーダの高さの向きになっていることに注意
       */
      val CASE_WIDTH = INSERTION_HEIGHT + RotaryEncoder.LEG_HEIGHT + 1.mm
   }

   val gear: Bevel get() {
      val (_, gear) = BackRotaryEncoderMediationGear.BevelGear.createPair()
      return gear.copy(
         referencePoint.translate(topVector, Shaft.GEAR_POSITION),
         frontVector,
         bottomVector
      )
   }

   val rotaryEncoder get(): RotaryEncoder {
      val e = RotaryEncoder(
         frontVector,
         bottomVector,
         referencePoint
            .translate(topVector, Shaft.HOLE_HEIGHT)
            .translate(bottomVector, RotaryEncoder.HEIGHT)
      )
      return e.rotate(Line3d(e.referencePoint, e.topVector), 90.deg)
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = BackRotaryEncoderGear(frontVector, bottomVector, referencePoint)
}

fun ScadParentObject.backRotaryEncoderGear(gear: BackRotaryEncoderGear): ScadObject {
   return (
      place(gear) {
         cylinder(BackRotaryEncoderGear.Shaft.HEIGHT, BackRotaryEncoderGear.Shaft.RADIUS)
      }
      + bevelGear(gear.gear)
      - rotaryEncoderKnobHole(gear.rotaryEncoder)
   )
}
