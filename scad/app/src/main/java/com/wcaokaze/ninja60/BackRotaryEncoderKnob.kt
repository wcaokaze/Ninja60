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
      /** 何番目の[AlphanumericColumn]にノブを配置するか */
      val COLUMN_INDEX = 3

      val RADIUS = 10.mm
      val HEIGHT = 14.mm
      val SHAFT_HOLE_RADIUS = 1.6.mm
      val GEAR_THICKNESS = 2.mm

      operator fun invoke(alphanumericPlate: AlphanumericPlate): BackRotaryEncoderKnob {
         val column = alphanumericPlate.columns[COLUMN_INDEX]
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

   val gear: Gear get() {
      val module = BackRotaryEncoderGear.MODULE
      val diameter = RADIUS * 2 - module * 2

      val toothCount = (diameter.numberAsMilliMeter/ module.numberAsMilliMeter).toInt()

      return Gear(
         BackRotaryEncoderGear.MODULE,
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
   fun ScadParentObject.locale(knob: BackRotaryEncoderKnob, children: ScadParentObject.() -> Unit): Translate {
      return locale(knob.referencePoint) {
         rotate(
            -Vector3d.Z_UNIT_VECTOR angleWith knob.bottomVector,
            -Vector3d.Z_UNIT_VECTOR vectorProduct knob.bottomVector,
            children
         )
      }
   }


   return (
      gear(knob.gear)
      + locale(knob) {
         cylinder(BackRotaryEncoderKnob.HEIGHT, BackRotaryEncoderKnob.RADIUS, `$fa`)
      }
      - locale(knob) {
         cylinder(
            BackRotaryEncoderKnob.HEIGHT * 3,
            BackRotaryEncoderKnob.SHAFT_HOLE_RADIUS,
            center = true, `$fa`)
      }
   )
}

// =============================================================================

/**
 * 奥側のロータリーエンコーダにつける歯車
 *
 * ロータリーエンコーダに挿すシャフト部分に1枚歯車がついた形状。
 */
data class BackRotaryEncoderGear(
   override val referencePoint: Point3d,
   /** 底面([referencePoint])から歯車までの距離 */
   val gearPosition: Size,
   val toothCount: Int,
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d
) : Transformable<BackRotaryEncoderGear> {
   companion object {
      val MODULE = 1.mm

      /** 歯車の暑さ */
      val GEAR_THICKNESS = 2.mm

      /** ロータリーエンコーダに挿す部分の高さ */
      val SHAFT_HEIGHT = RotaryEncoder.SHAFT_HEIGHT - 5.5.mm

      /** ロータリーエンコーダに挿す部分の穴の高さ */
      val SHAFT_HOLE_HEIGHT = SHAFT_HEIGHT

      /** ロータリーエンコーダに挿す部分の半径 */
      val SHAFT_RADIUS = RotaryEncoder.SHAFT_RADIUS + 2.mm

      /** ロータリーエンコーダを入れる際にケースに必要な穴の高さ */
      val INSERTION_HEIGHT = RotaryEncoder.LEG_HEIGHT + RotaryEncoder.HEIGHT + 0.5.mm

      /**
       * ロータリーエンコーダを入れる部分のケースの幅
       *
       * ロータリーエンコーダが横向きに設置されるので、
       * ケースの幅はロータリーエンコーダの高さの向きになっていることに注意
       */
      val CASE_WIDTH = INSERTION_HEIGHT + 1.mm

      operator fun invoke(alphanumericPlate: AlphanumericPlate, velocityRatio: Double): BackRotaryEncoderGear {
         val gear = gear(alphanumericPlate, velocityRatio)
         val gearAxis = Line3d(gear.referencePoint, gear.topVector)
         val caseLeftPlane = backRotaryEncoderCaseLeftPlane(alphanumericPlate, 0.mm)
         val caseSlopePlane = backRotaryEncoderCaseSlopePlane(alphanumericPlate, 0.mm)

         val mountPlatePlane = caseLeftPlane
            .translate(-caseLeftPlane.normalVector, INSERTION_HEIGHT)

         val rotaryEncoder = RotaryEncoder(
            caseSlopePlane.normalVector vectorProduct mountPlatePlane.normalVector,
            -mountPlatePlane.normalVector,
            mountPlatePlane intersection gearAxis
         )

         val shaftBottom = rotaryEncoder.referencePoint
            .translate(rotaryEncoder.topVector, RotaryEncoder.HEIGHT)
            .translate(rotaryEncoder.bottomVector, SHAFT_HOLE_HEIGHT)

         return BackRotaryEncoderGear(
            shaftBottom,
            Vector3d(shaftBottom, gear.referencePoint).norm,
            gear.toothCount,
            rotaryEncoder.frontVector,
            rotaryEncoder.bottomVector
         )
      }

      private fun gear(alphanumericPlate: AlphanumericPlate, velocityRatio: Double): Gear {
         val knob = BackRotaryEncoderKnob(alphanumericPlate)

         val gear = Gear(
            MODULE,
            toothCount = (velocityRatio * knob.gear.toothCount).toInt(),
            GEAR_THICKNESS,
            knob.gear.referencePoint,
            knob.gear.frontVector,
            knob.gear.bottomVector
         )

         val column = alphanumericPlate.columns[BackRotaryEncoderKnob.COLUMN_INDEX]
         val mostBackKey = column.keySwitches.first()
         val mostBackKeyLine = Line3d(mostBackKey.referencePoint, mostBackKey.rightVector)
            .translate(mostBackKey.topVector, KeySwitch.TRAVEL)
            .translate(mostBackKey.backVector, AlphanumericPlate.KEY_PLATE_SIZE.y / 2)
         val mostBackPoint = Plane3d(knob.gear.referencePoint, knob.gear.topVector)
            .intersection(mostBackKeyLine)

         /*
          * キーに干渉しないぎりぎり低い位置、
          * つまりgearの歯先円がmostBackKeyPointと接し、
          * gearの基準円がknob.gearの基準円と接するように配置する
          *
          *
          *     ケースの左側から
          *     見た図                 knob.gear
          *                                a
          *
          *                                +
          *                               /|
          *                              /α|
          *                             /  |
          *                            /   |
          *                           /    |
          *                          /     |
          *                         /      |
          *                        /       |
          *                       /        |
          *               gear   /         |
          *                  c  +----------|
          *                       --       |
          *                         --     |
          *                           --   |
          *                             --β|
          *                               -+
          *
          *                                b
          *                          mostBackPoint
          *
          * いろいろ解法はあろうかと思いますがここでは
          *
          *     ac sin α = bc sin β                   (1)
          *
          *     ab = ac cos α + bc cos β              (2)
          *
          * を利用します。
          *
          * (1)より
          *
          *     ac²sin²α = bc²sin²β
          *
          *     ac²sin²α = bc²(1 - cos²β)
          *
          *     bc²cos²β = bc² - ac²sin²α             (3)
          *
          * (2)より
          *
          *     ab - ac cos α = bc cos β
          *
          *     (ab - ac cos α)² = bc²cos²β           (4)
          *
          * (3), (4)より
          *
          *     (ab - ac cos α)² = bc² - ac²sin²α
          *
          * この方程式を解くと
          *
          *                ac² - bc² + ab²
          *     α = cos⁻¹ -----------------
          *                    2 ab ac
          *
          * が得られる。
          */

         operator fun Int.times(size: Size) = size * this
         operator fun Size.times(another: Size) = Size(numberAsMilliMeter * another.numberAsMilliMeter)
         fun Size.square() = this * this

         val ac = knob.gear distance gear
         val bc = gear.addendumRadius
         val abVector = Vector3d(knob.gear.referencePoint, mostBackPoint)
         val ab = abVector.norm

         val acVector = abVector.rotate(
            knob.gear.topVector,
            acos(
               ac.square() - bc.square() + ab.square(),
               2 * ab * ac
            )
         )

         // knobの歯車とここで必要な歯車は向きが逆なので裏返します
         // (Gearは実は点対称ではなく底面にreferencePointがある)
         return gear
            .translate(acVector, ac)
            .let {
               it.rotate(
                  axis = Line3d(
                     it.referencePoint.translate(it.topVector, it.thickness / 2),
                     it.frontVector
                  ),
                  angle = Angle.PI
               )
            }
      }
   }

   val rotaryEncoder get() = RotaryEncoder(
      frontVector,
      bottomVector,
      referencePoint
         .translate(topVector, SHAFT_HOLE_HEIGHT)
         .translate(bottomVector, RotaryEncoder.HEIGHT)
   )

   val gear get() = Gear(
      MODULE,
      toothCount,
      GEAR_THICKNESS,
      referencePoint.translate(topVector, gearPosition),
      frontVector,
      bottomVector
   )

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = BackRotaryEncoderGear(referencePoint, gearPosition, toothCount, frontVector, bottomVector)
}

fun ScadParentObject.backRotaryEncoderGear(gear: BackRotaryEncoderGear): ScadObject {
   return (
      locale(gear.referencePoint) {
         rotate(
            -Vector3d.Z_UNIT_VECTOR angleWith gear.bottomVector,
            -Vector3d.Z_UNIT_VECTOR vectorProduct gear.bottomVector,
         ) {
            cylinder(BackRotaryEncoderGear.SHAFT_HEIGHT, BackRotaryEncoderGear.SHAFT_RADIUS, `$fa`)
         }
      }
      + gear(gear.gear)
   )
}
