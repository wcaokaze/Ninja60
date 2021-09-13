package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * 奥側のロータリーエンコーダのノブ。
 * ここのノブは直接ロータリーエンコーダに挿しておらず、
 * さらに奥にあるロータリーエンコーダに歯車で伝達する仕組みであることに注意
 */
data class BackRotaryEncoderKnob(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : Transformable<BackRotaryEncoderKnob> {
   companion object {
      val RADIUS = 10.mm
      val HEIGHT = 14.mm
      val SHAFT_HOLE_RADIUS = 1.6.mm
      val GEAR_THICKNESS = 2.mm

      operator fun invoke(alphanumericPlate: AlphanumericPlate): BackRotaryEncoderKnob {
         val column = alphanumericPlate.columns[3]
         val mostBackKey = column.keySwitches.first()
         val keyBackPoint = mostBackKey.referencePoint
            .translate(mostBackKey.backVector, AlphanumericPlate.KEY_PLATE_SIZE.y)
         val knobCenter = keyBackPoint
            .translate(mostBackKey.topVector, RADIUS * 2)
            .translate(mostBackKey.leftVector, HEIGHT / 2)
            .translate(mostBackKey.topVector, 4.mm)
            .translate(mostBackKey.backVector, 2.mm)

         return BackRotaryEncoderKnob(
            mostBackKey.topVector,
            mostBackKey.leftVector,
            knobCenter
         )
      }
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = BackRotaryEncoderKnob(frontVector, bottomVector, referencePoint)
}

fun ScadParentObject.backRotaryEncoderKnob(knob: BackRotaryEncoderKnob): ScadObject {
   val module = BackRotaryEncoderGear.MODULE
   val diameter = BackRotaryEncoderKnob.RADIUS * 2 - module * 2

   val toothCount = (diameter.numberAsMilliMeter/ module.numberAsMilliMeter).toInt()

   val gear = Gear(
      BackRotaryEncoderGear.MODULE,
      toothCount,
      BackRotaryEncoderKnob.GEAR_THICKNESS,
      Point3d.ORIGIN.translate(z = -BackRotaryEncoderKnob.GEAR_THICKNESS),
      -Vector3d.Y_UNIT_VECTOR, -Vector3d.Z_UNIT_VECTOR
   )

   return union {
      locale(knob.referencePoint) {
         rotate(
            -Vector3d.Z_UNIT_VECTOR angleWith knob.bottomVector,
            -Vector3d.Z_UNIT_VECTOR vectorProduct knob.bottomVector
         ) {
            (
               gear(gear)
               + cylinder(BackRotaryEncoderKnob.HEIGHT, BackRotaryEncoderKnob.RADIUS, `$fa`)
               - cylinder(
                  BackRotaryEncoderKnob.HEIGHT * 3,
                  BackRotaryEncoderKnob.SHAFT_HOLE_RADIUS,
                  center = true, `$fa`)
            )
         }
      }
   }
}

/**
 * 奥側のロータリーエンコーダにつける歯車。
 */
class BackRotaryEncoderGear(
) {
   companion object {
      val MODULE = 1.mm
      val HEIGHT = RotaryEncoder.SHAFT_HEIGHT - 1.mm
      val HOLE_HEIGHT = HEIGHT - 2.mm
   }
}
