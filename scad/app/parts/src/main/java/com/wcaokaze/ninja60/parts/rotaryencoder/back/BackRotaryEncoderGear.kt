package com.wcaokaze.ninja60.parts.rotaryencoder.back

import com.wcaokaze.scadwriter.linearalgebra.*
import com.wcaokaze.ninja60.parts.rotaryencoder.*
import com.wcaokaze.ninja60.parts.rotaryencoder.gear.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.ninja60.shared.scadutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * ロータリーエンコーダに挿すシャフト部分に1枚[BevelGear]がついた形状。
 */
data class BackRotaryEncoderGear(
   override val referencePoint: Point3d,
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   val shaftHeight: Size,
   val shaftRadius: Size,
   val shaftHoleDepth: Size
) : TransformableDefaultImpl<BackRotaryEncoderGear> {
   object Gear {
      val TOOTH_COUNT = 16
   }

   object Shaft {
      /**
       * 歯車の位置。[referencePoint]から[topVector]方向の距離
       */
      val GEAR_POSITION = 0.mm
   }

   val gear: BevelGear get() {
      val (_, gear) = BackRotaryEncoderMediationGear.BevelGear.createPair()
      return gear.copy(
         referencePoint.translate(topVector, Shaft.GEAR_POSITION),
         frontVector,
         bottomVector
      )
   }

   val rotaryEncoder: RotaryEncoder get() {
      val e = RotaryEncoder(
         frontVector,
         bottomVector,
         referencePoint
            .translate(topVector, shaftHoleDepth)
            .translate(bottomVector, RotaryEncoder.HEIGHT)
      )
      return e.rotate(Line3d(e.referencePoint, e.topVector), 90.deg)
   }

   override fun copy(
      referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d
   ) = BackRotaryEncoderGear(
      referencePoint, frontVector, bottomVector,
      shaftRadius, shaftRadius, shaftHoleDepth
   )
}

fun ScadParentObject.backRotaryEncoderGear(gear: BackRotaryEncoderGear): ScadObject {
   return (
      place(gear) {
         cylinder(gear.shaftHeight, gear.shaftRadius)
      }
      + bevelGear(gear.gear)
      - rotaryEncoderKnobHole(gear.rotaryEncoder)
   )
}
