package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

class LeftOuterRotaryEncoderKnob(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : Transformable<LeftOuterRotaryEncoderKnob> {
   companion object {
      /** 奥から何番目の[KeySwitch]にノブを配置するか */
      val ROW_INDEX = 2

      val MODULE = 1.mm
      val RADIUS = 30.mm
      val HEIGHT = 8.mm
      val THICKNESS = 2.mm

      val INNER_KNOB_DEPTH = 1.5.mm

      val INTERNAL_GEAR_TOOTH_COUNT = gearToothCount(
         MODULE,
         (RADIUS - THICKNESS) * 2
      )

      val PROTUBERANCE_COUNT = 7
      val PROTUBERANCE_RADIUS = 0.75.mm

      val SKIDPROOF_COUNT = 14
      val SKIDPROOF_RADIUS = 0.75.mm

      operator fun invoke(alphanumericPlate: AlphanumericPlate): LeftOuterRotaryEncoderKnob {
         val leftmostColumn = alphanumericPlate.columns.first()
         val keySwitch = leftmostColumn.keySwitches[ROW_INDEX]

         val keycapTop = keySwitch.referencePoint
            .translate(keySwitch.topVector,
               KeySwitch.TOP_HEIGHT + KeySwitch.STEM_HEIGHT + Keycap.THICKNESS)

         val tangencyPoint = alphanumericPlate.leftmostPlane intersection
               Line3d(keycapTop, keySwitch.leftVector)

         return LeftOuterRotaryEncoderKnob(
               -alphanumericPlate.leftmostPlane.normalVector
                     vectorProduct -Vector3d.Z_UNIT_VECTOR,
               -Vector3d.Z_UNIT_VECTOR,
               tangencyPoint
            )
            .let { it.translate(it.leftVector, RADIUS + 2.mm) }
            .let { it.translate(it.backVector, keyPitch.y / 3) }
            .let { it.translate(it.bottomVector, HEIGHT) }
      }
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

      return gear.translate(rightVector, internalGear distance gear.gear)
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
         LeftOuterRotaryEncoderKnob.RADIUS,
         `$fa`
      )
   }

   fun ScadParentObject.innerKnobHole(): ScadObject {
      return translate(
         z = LeftOuterRotaryEncoderKnob.HEIGHT
               - LeftOuterRotaryEncoderKnob.INNER_KNOB_DEPTH
      ) {
         cylinder(
            LeftOuterRotaryEncoderKnob.INNER_KNOB_DEPTH * 2,
            LeftInnerRotaryEncoderKnob.RADIUS + 0.7.mm,
            `$fa`
         )
      }
   }

   fun ScadParentObject.innerRotaryEncoderShaftHole(): ScadObject {
      return cylinder(
         LeftOuterRotaryEncoderKnob.HEIGHT,
         RotaryEncoder.SHAFT_RADIUS + 0.5.mm,
         `$fa`
      )
   }

   fun ScadParentObject.internalCave(): ScadObject {
      return cylinder(
         Vector3d(
            leftOuterRotaryEncoderKnob.referencePoint,
            leftOuterRotaryEncoderKnob.internalGear.referencePoint
         ).norm,
         LeftOuterRotaryEncoderKnob.RADIUS - LeftOuterRotaryEncoderKnob.THICKNESS,
         `$fa`
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
      locale(leftOuterRotaryEncoderKnob.referencePoint) {
         rotate(
            -Vector3d.Z_UNIT_VECTOR angleWith leftOuterRotaryEncoderKnob.bottomVector,
            -Vector3d.Z_UNIT_VECTOR vectorProduct leftOuterRotaryEncoderKnob.bottomVector
         ) {
            (
               mainBody()
               - innerKnobHole()
               - innerRotaryEncoderShaftHole()
               - internalCave()
               + protuberance()
               + skidproof()
            )
         }
      }
      - internalGear(leftOuterRotaryEncoderKnob.internalGear)
   )
}

private fun ScadParentObject.repeatRotation(
   count: Int,
   child: ScadParentObject.() -> Unit
): ScadObject {
   val twoPi = Angle.PI * 2

   return union {
      for (a in 0.0.rad..twoPi step twoPi / count) {
         rotate(z = a) {
            child()
         }
      }
   }
}

// =============================================================================

class LeftOuterRotaryEncoderGear(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : Transformable<LeftOuterRotaryEncoderGear> {
   companion object {
      val HEIGHT = 4.mm

      val RADIUS = (
            LeftOuterRotaryEncoderKnob.RADIUS
                  - LeftOuterRotaryEncoderKnob.THICKNESS
                  - RotaryEncoder.SHAFT_RADIUS
         ) / 2 - 0.5.mm

      val TOOTH_COUNT = gearToothCount(
         LeftOuterRotaryEncoderKnob.MODULE,
         RADIUS * 2
      )
   }

   val gear get() = Gear(
      LeftOuterRotaryEncoderKnob.MODULE,
      TOOTH_COUNT,
      HEIGHT,
      referencePoint,
      frontVector, bottomVector
   )

   val rotaryEncoder: RotaryEncoder get() = RotaryEncoder(
      frontVector,
      bottomVector,
      referencePoint.translate(topVector, HEIGHT - RotaryEncoder.HEIGHT)
   )

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
      = LeftOuterRotaryEncoderGear(frontVector, bottomVector, referencePoint)
}

fun ScadParentObject.leftOuterRotaryEncoderGear(
   leftOuterRotaryEncoderGear: LeftOuterRotaryEncoderGear
): ScadObject {
   return difference {
      gear(leftOuterRotaryEncoderGear.gear)
      rotaryEncoderKnobHole(leftOuterRotaryEncoderGear.rotaryEncoder)
   }
}

private fun gearToothCount(module: Size, diameter: Size) = (
   (diameter - module * 2).numberAsMilliMeter
      / module.numberAsMilliMeter
).toInt()
