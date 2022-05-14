package com.wcaokaze.ninja60.parts.rotaryencoder.left

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.parts.rotaryencoder.*
import com.wcaokaze.ninja60.parts.rotaryencoder.gear.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.ninja60.shared.scadutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

class LeftOuterRotaryEncoderKnob(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : TransformableDefaultImpl<LeftOuterRotaryEncoderKnob> {
   companion object {
      val MODULE = 1.mm
      val RADIUS = 30.mm
      val HEIGHT = 8.mm
      val THICKNESS = 2.mm

      val INNER_KNOB_DEPTH = 1.5.mm

      val INTERNAL_GEAR_TOOTH_COUNT = Gear.toothCount(
         MODULE,
         (RADIUS - THICKNESS) * 2
      )

      val PROTUBERANCE_COUNT = 7
      val PROTUBERANCE_RADIUS = 0.75.mm

      val SKIDPROOF_COUNT = 14
      val SKIDPROOF_RADIUS = 0.75.mm
   }

   val internalGear: InternalGear get() {
      return InternalGear(
         MODULE, INTERNAL_GEAR_TOOTH_COUNT, HEIGHT - THICKNESS,
         referencePoint,
         frontVector, bottomVector
      )
   }

   val gear: LeftOuterRotaryEncoderGear get() {
      val gear = LeftOuterRotaryEncoderGear(
         internalGear.frontVector,
         internalGear.bottomVector,
         internalGear.referencePoint
            .translate(topVector, HEIGHT - INNER_KNOB_DEPTH - THICKNESS - LeftOuterRotaryEncoderGear.HEIGHT - 0.5.mm)
      )

      return gear.translate(rightVector, internalGear idealDistance gear.gear)
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
      =  LeftOuterRotaryEncoderKnob(frontVector, bottomVector, referencePoint)
}

fun ScadParentObject.leftOuterRotaryEncoderKnob(
   leftOuterRotaryEncoderKnob: LeftOuterRotaryEncoderKnob
): ScadObject {
   fun ScadParentObject.mainBody(): ScadObject {
      return cylinder(
         LeftOuterRotaryEncoderKnob.HEIGHT,
         LeftOuterRotaryEncoderKnob.RADIUS
      )
   }

   fun ScadParentObject.innerKnobHole(): ScadObject {
      return translate(
         z = LeftOuterRotaryEncoderKnob.HEIGHT
               - LeftOuterRotaryEncoderKnob.INNER_KNOB_DEPTH
      ) {
         cylinder(
            LeftOuterRotaryEncoderKnob.INNER_KNOB_DEPTH * 2,
            LeftInnerRotaryEncoderKnob.RADIUS + 0.7.mm
         )
      }
   }

   fun ScadParentObject.innerRotaryEncoderShaftHole(): ScadObject {
      return cylinder(
         LeftOuterRotaryEncoderKnob.HEIGHT,
         RotaryEncoder.SHAFT_RADIUS + 0.5.mm
      )
   }

   fun ScadParentObject.internalCave(): ScadObject {
      return cylinder(
         leftOuterRotaryEncoderKnob.referencePoint
               distance leftOuterRotaryEncoderKnob.internalGear.referencePoint,
         LeftOuterRotaryEncoderKnob.RADIUS - LeftOuterRotaryEncoderKnob.THICKNESS
      )
   }

   fun ScadParentObject.protuberance(): ScadObject {
      return repeatRotation(LeftOuterRotaryEncoderKnob.PROTUBERANCE_COUNT) {
         translate(x = leftOuterRotaryEncoderKnob.internalGear.bottomRadius
               + LeftOuterRotaryEncoderKnob.PROTUBERANCE_RADIUS)
         {
            linearProtuberance(
               LeftOuterRotaryEncoderKnob.RADIUS
                     - leftOuterRotaryEncoderKnob.internalGear.bottomRadius
                     - LeftOuterRotaryEncoderKnob.PROTUBERANCE_RADIUS,
               LeftOuterRotaryEncoderKnob.PROTUBERANCE_RADIUS
            )
         }
         translate(LeftOuterRotaryEncoderKnob.RADIUS) {
            rotate(y = (-90).deg) {
               linearProtuberance(
                  LeftOuterRotaryEncoderKnob.HEIGHT - 1.5.mm,
                  LeftOuterRotaryEncoderKnob.PROTUBERANCE_RADIUS
               )
            }
         }
      }
   }

   fun ScadParentObject.skidproof(): ScadObject {
      return repeatRotation(LeftOuterRotaryEncoderKnob.SKIDPROOF_COUNT) {
         translate(
            x = LeftInnerRotaryEncoderKnob.RADIUS + 7.mm
                  + LeftOuterRotaryEncoderKnob.SKIDPROOF_RADIUS,
            z = LeftOuterRotaryEncoderKnob.HEIGHT
         ) {
            linearProtuberance(
               LeftOuterRotaryEncoderKnob.RADIUS
                     - LeftInnerRotaryEncoderKnob.RADIUS
                     - 7.mm
                     - LeftOuterRotaryEncoderKnob.SKIDPROOF_RADIUS
                     - 1.mm,
               LeftOuterRotaryEncoderKnob.SKIDPROOF_RADIUS
            )
         }
      }
   }

   return (
      place(leftOuterRotaryEncoderKnob) {
         (
            mainBody()
            - innerKnobHole()
            - innerRotaryEncoderShaftHole()
            - internalCave()
            + protuberance()
            + skidproof()
         )
      }
      - internalGear(leftOuterRotaryEncoderKnob.internalGear)
   )
}
