package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
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
      /** 何番目の[AlphanumericColumn]にノブを配置するか */
      val COLUMN_INDEX = 3

      val RADIUS = 10.mm
      val HEIGHT = 15.mm
      val SHAFT_HOLE_RADIUS = 1.1.mm
      val GEAR_THICKNESS = 2.mm

      val SKIDPROOF_COUNT = 32
      val SKIDPROOF_RADIUS = 0.25.mm

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

      fun createPair(): Pair<com.wcaokaze.ninja60.BevelGear, com.wcaokaze.ninja60.BevelGear> {
         return com.wcaokaze.ninja60.BevelGear.createPair(
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

      operator fun invoke(
         alphanumericPlate: AlphanumericPlate,
         alphanumericCasePlane: Plane3d
      ): BackRotaryEncoderMediationGear {
         val knob = BackRotaryEncoderKnob(alphanumericPlate)

         val gear = Gear(
            SpurGear.MODULE,
            SpurGear.TOOTH_COUNT,
            SpurGear.THICKNESS,
            knob.gear.referencePoint,
            knob.gear.frontVector,
            knob.gear.bottomVector
         )

         // 歯車を配置する平面
         val gearPlane = Plane3d(knob.gear.referencePoint, knob.gear.topVector)

         // ケースの壁とgearPlaneとの交線
         val caseLine = alphanumericCasePlane intersection gearPlane

         // caseLineを歯車の半径分並行移動した直線。
         // この直線上に歯車の中心を配置すると歯車とケースが接することになる
         val gearLine = caseLine.translate(
            gearPlane.normalVector vectorProduct caseLine.vector,
            gear.addendumRadius
         )

         // gearLine上にあってknobからの距離が適切な点
         // すなわち半径idealDistanceの円との交点が歯車の中心となる
         /*
         val p = Circle3d(
               knob.referencePoint,
               normalVector = knob.topVector,
               radius = knob.gear idealDistance gear
            )
            .intersection(gearLine)
            .minByOrNull { it.z }
         */
         // のだけど、さすがに三次元空間の円の計算はしんどすぎるので
         // ここは計算機のパワーを借りて力技でいきます

         val idealDistance = knob.gear idealDistance gear

         val startPoint = Plane3d(knob.gear.referencePoint, gearLine.vector)
            .intersection(gearLine)

         val p = (
               startPoint..startPoint.translate(gearLine.vector, idealDistance * 1.5)
               step 0.05.mm
            )
            .first { it distance knob.gear.referencePoint > idealDistance }

         return BackRotaryEncoderMediationGear(
            gear.frontVector, gear.bottomVector, p)
      }
   }

   val spurGear get() = Gear(
      SpurGear.MODULE,
      SpurGear.TOOTH_COUNT,
      SpurGear.THICKNESS,
      referencePoint,
      frontVector,
      bottomVector
   )

   val bevelGear: com.wcaokaze.ninja60.BevelGear get() {
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
 * ロータリーエンコーダに挿すシャフト部分に1枚[BevelGear]がついた形状。
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

   companion object {
      operator fun invoke(
         mediationGear: BackRotaryEncoderMediationGear,
         alphanumericCasePlane: Plane3d
      ): BackRotaryEncoderGear {
         var (mediationGearPositionRef, gear) =
            BackRotaryEncoderMediationGear.BevelGear.createPair()

         run {
            val d = mediationGear.bevelGear.referencePoint - mediationGearPositionRef.referencePoint
            mediationGearPositionRef = mediationGearPositionRef.translate(d)
            gear = gear.translate(d)
         }

         run {
            val a = mediationGearPositionRef.topVector angleWith mediationGear.bevelGear.topVector
            val v = Line3d(
               mediationGearPositionRef.referencePoint,
               mediationGearPositionRef.topVector vectorProduct mediationGear.bevelGear.topVector
            )
            mediationGearPositionRef = mediationGearPositionRef.rotate(v, a)
            gear = gear.rotate(v, a)
         }

         run {
            val alphanumericCaseLine
               = Plane3d(gear.referencePoint, mediationGear.bevelGear.topVector)
               .intersection(alphanumericCasePlane)
            val a = gear.topVector angleWith alphanumericCaseLine.vector
            val v = mediationGearPositionRef.topVectorLine
            mediationGearPositionRef = mediationGearPositionRef.rotate(v, a)
            gear = gear.rotate(v, a)
         }

         return BackRotaryEncoderGear(
            gear.frontVector,
            gear.bottomVector,
            gear.referencePoint.translate(gear.bottomVector, Shaft.GEAR_POSITION)
         )
      }
   }

   val gear: BevelGear get() {
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
