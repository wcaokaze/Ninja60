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
      val HEIGHT = 14.mm
      val THICKNESS = 2.mm

      val INNER_KNOB_DEPTH = 1.5.mm

      val INTERNAL_GEAR_TOOTH_COUNT = gearToothCount(
         MODULE,
         (RADIUS - THICKNESS - LeftOuterRotaryEncoderGear.SAUCER_THICKNESS) * 2
      )

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
      val h = LeftOuterRotaryEncoderGear.GEAR_THICKNESS + 0.5.mm

      return InternalGear(
         MODULE, INTERNAL_GEAR_TOOTH_COUNT, h,
         referencePoint
            .translate(topVector, HEIGHT - INNER_KNOB_DEPTH - THICKNESS - h),
         frontVector, bottomVector
      )
   }

   val gears: List<LeftOuterRotaryEncoderGear> get() {
      val gear = LeftOuterRotaryEncoderGear(
         internalGear.frontVector,
         internalGear.bottomVector,
         internalGear.referencePoint
            .translate(
               internalGear.bottomVector,
               LeftOuterRotaryEncoderGear.SAUCER_THICKNESS
            )
      )

      val d = internalGear distance gear.gear

      return listOf(
         gear.translate(rightVector.rotate(bottomVector, 360.deg * 0 / 3), d),
         gear.translate(rightVector.rotate(bottomVector, 360.deg * 1 / 3), d),
         gear.translate(rightVector.rotate(bottomVector, 360.deg * 2 / 3), d)
      )
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
      =  LeftOuterRotaryEncoderKnob(frontVector, bottomVector, referencePoint)
}

fun ScadParentObject.leftOuterRotaryEncoderKnob(
   leftOuterRotaryEncoderKnob: LeftOuterRotaryEncoderKnob
): ScadObject {
   return (
      locale(leftOuterRotaryEncoderKnob.referencePoint) {
         rotate(
            -Vector3d.Z_UNIT_VECTOR angleWith leftOuterRotaryEncoderKnob.bottomVector,
            -Vector3d.Z_UNIT_VECTOR vectorProduct leftOuterRotaryEncoderKnob.bottomVector
         ) {
            difference {
               cylinder(
                  LeftOuterRotaryEncoderKnob.HEIGHT,
                  LeftOuterRotaryEncoderKnob.RADIUS,
                  `$fa`
               )

               translate(
                  z = LeftOuterRotaryEncoderKnob.HEIGHT
                        - LeftOuterRotaryEncoderKnob.INNER_KNOB_DEPTH
               ) {
                  cylinder(
                     LeftOuterRotaryEncoderKnob.INNER_KNOB_DEPTH * 2,
                     LeftInnerRotaryEncoderKnob.RADIUS + 0.7.mm,
                     `$fa`
                  )
               }

               cylinder(
                  LeftOuterRotaryEncoderKnob.HEIGHT,
                  RotaryEncoder.SHAFT_RADIUS + 0.5.mm,
                  `$fa`
               )

               cylinder(
                  Vector3d(
                     leftOuterRotaryEncoderKnob.referencePoint,
                     leftOuterRotaryEncoderKnob.internalGear.referencePoint
                  ).norm,
                  LeftOuterRotaryEncoderKnob.RADIUS - LeftOuterRotaryEncoderKnob.THICKNESS,
                  `$fa`
               )
            }
         }
      }
      - internalGear(leftOuterRotaryEncoderKnob.internalGear)
   )
}

// =============================================================================

class LeftOuterRotaryEncoderGear(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : Transformable<LeftOuterRotaryEncoderGear> {
   companion object {
      val HEIGHT = 8.mm

      val RADIUS = (
            LeftOuterRotaryEncoderKnob.RADIUS
                  - LeftOuterRotaryEncoderKnob.THICKNESS
                  - RotaryEncoder.SHAFT_RADIUS
         ) / 2 - 0.5.mm

      val SAUCER_THICKNESS = 2.mm

      val GEAR_THICKNESS = HEIGHT - SAUCER_THICKNESS

      val TOOTH_COUNT = gearToothCount(
         LeftOuterRotaryEncoderKnob.MODULE,
         (RADIUS - SAUCER_THICKNESS) * 2
      ) + 1
   }

   val gear get() = Gear(
      LeftOuterRotaryEncoderKnob.MODULE,
      TOOTH_COUNT,
      GEAR_THICKNESS,
      referencePoint.translate(topVector, SAUCER_THICKNESS),
      frontVector, bottomVector
   )

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
      = LeftOuterRotaryEncoderGear(frontVector, bottomVector, referencePoint)
}

fun ScadParentObject.leftOuterRotaryEncoderGear(
   leftOuterRotaryEncoderGear: LeftOuterRotaryEncoderGear
): ScadObject {
   return union {
      locale(leftOuterRotaryEncoderGear.referencePoint) {
         rotate(
            -Vector3d.Z_UNIT_VECTOR angleWith leftOuterRotaryEncoderGear.bottomVector,
            -Vector3d.Z_UNIT_VECTOR vectorProduct leftOuterRotaryEncoderGear.bottomVector
         ) {
            cylinder(
               LeftOuterRotaryEncoderGear.SAUCER_THICKNESS,
               LeftOuterRotaryEncoderGear.RADIUS,
               `$fa`
            )
         }
      }

      gear(leftOuterRotaryEncoderGear.gear)
   }
}

private fun gearToothCount(module: Size, diameter: Size) = (
   (diameter - module * 2).numberAsMilliMeter
      / module.numberAsMilliMeter
).toInt()
