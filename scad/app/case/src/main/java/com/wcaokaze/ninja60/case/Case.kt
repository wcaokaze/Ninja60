package com.wcaokaze.ninja60.case

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.case.scad.*
import com.wcaokaze.ninja60.parts.key.*
import com.wcaokaze.ninja60.parts.key.alphanumeric.*
import com.wcaokaze.ninja60.parts.key.thumb.*
import com.wcaokaze.ninja60.parts.rotaryencoder.back.*
import com.wcaokaze.ninja60.parts.rotaryencoder.front.*
import com.wcaokaze.ninja60.parts.rotaryencoder.left.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.scadwriter.foundation.*

data class Case(
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   override val referencePoint: Point3d
) : Transformable<Case> {
   constructor() : this(
      -Vector3d.Y_UNIT_VECTOR,
      -Vector3d.Z_UNIT_VECTOR,
      Point3d.ORIGIN
   )

   companion object {
      private val THUMB_KEY_PITCH = 19.2.mm

      val ALPHANUMERIC_FRONT_LEFT_MARGIN = 9.mm
      val ALPHANUMERIC_FRONT_RIGHT_MARGIN = 0.mm

      val FRONT_ROTARY_ENCODER_KEY_CASE_HEIGHT = 9.mm
      val THUMB_HOME_KEY_CASE_HEIGHT = 4.mm
   }

   val alphanumericPlate: AlphanumericPlate get() {
      return AlphanumericPlate(frontVector, bottomVector, referencePoint)
         .transform { rotate(frontVectorLine, 15.deg) }
         .translate(backVector, 69.mm)
         .translate(topVector, 65.mm)
   }

   val thumbHomeKey: KeySwitch get() {
      return KeySwitch(referencePoint, bottomVector, frontVector)
         .transform { rotate(backVectorLine, 65.deg) }
         .transform { rotate(leftVectorLine, 1.deg) }
         .transform { rotate(bottomVectorLine, 14.deg) }
         .translate(rightVector, 25.mm)
         .translate(backVector, 17.mm)
         .translate(topVector, 29.mm)
   }

   val thumbPlate: ThumbPlate get() {
      val leftmostKey = thumbHomeKey
         .transform { translate(rightVector, THUMB_KEY_PITCH * thumbHomeKey.layoutSize.x) }
         .transform {
            rotate(
               frontVectorLine.translate(topVector, KeySwitch.KEYCAP_SURFACE_HEIGHT)
                              .translate(leftVector, THUMB_KEY_PITCH * layoutSize.x / 2),
               69.deg
            )
         }

      return ThumbPlate(
         leftmostKey.referencePoint, layoutRadius = 60.mm, THUMB_KEY_PITCH,
         leftmostKey.frontVector, leftmostKey.bottomVector
      )
   }

   val frontRotaryEncoderKnob get() = FrontRotaryEncoderKnob(alphanumericPlate, alphanumericTopPlaneLeft(alphanumericPlate, offset = 0.mm))
   val backRotaryEncoderKnob get() = BackRotaryEncoderKnob(alphanumericPlate)
   val backRotaryEncoderMediationGear get() = BackRotaryEncoderMediationGear(alphanumericPlate, alphanumericBackSlopePlane(alphanumericPlate, offset = 1.7.mm))
   val backRotaryEncoderGear get() = BackRotaryEncoderGear(backRotaryEncoderMediationGear, alphanumericBackSlopePlane(alphanumericPlate, offset = 1.7.mm))
   val leftOuterRotaryEncoderKnob get() = LeftOuterRotaryEncoderKnob(this)
   val leftInnerRotaryEncoderKnob get() = LeftInnerRotaryEncoderKnob(leftOuterRotaryEncoderKnob)

   val frontRotaryEncoderKey get() = FrontRotaryEncoderKey(frontRotaryEncoderKnob)

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = Case(frontVector, bottomVector, referencePoint)
}
