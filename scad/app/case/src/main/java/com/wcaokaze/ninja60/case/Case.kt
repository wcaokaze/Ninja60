package com.wcaokaze.ninja60.case

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.case.scad.*
import com.wcaokaze.ninja60.parts.key.*
import com.wcaokaze.ninja60.parts.key.alphanumeric.*
import com.wcaokaze.ninja60.parts.key.thumb.*
import com.wcaokaze.ninja60.parts.rotaryencoder.back.*
import com.wcaokaze.ninja60.parts.rotaryencoder.front.*
import com.wcaokaze.ninja60.parts.rotaryencoder.gear.*
import com.wcaokaze.ninja60.parts.rotaryencoder.left.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.ninja60.shared.scadutil.*
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

      /** 何番目の[AlphanumericColumn]に[FrontRotaryEncoderKnob]を配置するか */
      const val FRONT_ROTARY_ENCODER_COLUMN_INDEX = 3
      /** 何番目の[AlphanumericColumn]に[BackRotaryEncoderKnob]を配置するか */
      const val BACK_ROTARY_ENCODER_COLUMN_INDEX = 3
      /** 奥から何番目の[KeySwitch]に[LeftOuterRotaryEncoderKnob]を配置するか */
      const val LEFT_ROTARY_ENCODER_ROW_INDEX = 2

      /**
       * [FrontRotaryEncoderKey]と[FrontRotaryEncoderKnob]の
       * [bottomVector][Transformable.bottomVector]方向のズレ。
       *
       * [referencePoint]基準。つまりノブの表面とかキーの表面ではなく、
       * ノブの底面位置からキースイッチのマウントプレート位置までの
       * Z軸上の距離。
       */
      val FRONT_ROTARY_ENCODER_KEY_Z_OFFSET = 8.8.mm

      val ALPHANUMERIC_FRONT_LEFT_MARGIN = 9.mm
      val ALPHANUMERIC_FRONT_RIGHT_MARGIN = 0.mm

      val FRONT_ROTARY_ENCODER_KEY_CASE_HEIGHT = 9.mm
      val THUMB_HOME_KEY_CASE_HEIGHT = 4.mm

      val BACK_ROTARY_ENCODER_CASE_DEPTH = 15.mm
      val BACK_ROTARY_ENCODER_CASE_MARGIN_SPACE = 1.5.mm
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

   val frontRotaryEncoderKnob: FrontRotaryEncoderKnob get() {
      val alphanumericTopPlane = alphanumericTopPlaneLeft(alphanumericPlate, offset = 0.mm)

      val column = alphanumericPlate.columns[FRONT_ROTARY_ENCODER_COLUMN_INDEX]
      val columnPlane = Plane3d(column.referencePoint, column.rightVector)

      val mostFrontKey = column.keySwitches.last()
      val mostFrontKeycapTopPlane = Plane3d(mostFrontKey.referencePoint, mostFrontKey.topVector)
         .translate(mostFrontKey.topVector, KeySwitch.KEYCAP_SURFACE_HEIGHT)

      val knobCenter = (
            alphanumericTopPlane
               .translate(-alphanumericTopPlane.normalVector, 2.mm)
         ) intersection (
            columnPlane
         ) intersection (
            mostFrontKeycapTopPlane
               .translate(mostFrontKey.bottomVector,
                  FrontRotaryEncoderKnob.RADIUS
               )
               .translate(mostFrontKey.bottomVector, KeySwitch.TRAVEL)
               .translate(mostFrontKey.bottomVector, 2.mm)
         )

      return FrontRotaryEncoderKnob(
         column.frontVector vectorProduct alphanumericTopPlane.normalVector,
         -alphanumericTopPlane.normalVector,
         knobCenter
      )
   }

   val backRotaryEncoderKnob: BackRotaryEncoderKnob get() {
      val column = alphanumericPlate.columns[BACK_ROTARY_ENCODER_COLUMN_INDEX]
      val mostBackKey = column.keySwitches.first()
      val keyBackPoint = mostBackKey.referencePoint
         .translate(mostBackKey.backVector, AlphanumericPlate.KEY_PLATE_SIZE.y)
      val knobCenter = keyBackPoint
         .translate(mostBackKey.topVector, BackRotaryEncoderKnob.RADIUS * 2)
         .translate(mostBackKey.leftVector, BackRotaryEncoderKnob.HEIGHT / 2)
         .translate(mostBackKey.topVector, 4.mm)
         .translate(mostBackKey.backVector, 2.mm)

      return BackRotaryEncoderKnob(
         mostBackKey.topVector,
         mostBackKey.leftVector,
         knobCenter
      )
   }

   val backRotaryEncoderMediationGear: BackRotaryEncoderMediationGear get() {
      val alphanumericCasePlane = alphanumericBackSlopePlane(alphanumericPlate, offset = 1.7.mm)

      val knob = backRotaryEncoderKnob

      val gear = Gear(
         BackRotaryEncoderMediationGear.SpurGear.MODULE,
         BackRotaryEncoderMediationGear.SpurGear.TOOTH_COUNT,
         BackRotaryEncoderMediationGear.SpurGear.THICKNESS,
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

      return BackRotaryEncoderMediationGear(gear.frontVector, gear.bottomVector, p)
   }

   val backRotaryEncoderGear: BackRotaryEncoderGear get() {
      val mediationGear = backRotaryEncoderMediationGear
      val alphanumericCasePlane = alphanumericBackSlopePlane(alphanumericPlate, offset = 1.7.mm)

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
         gear.referencePoint.translate(gear.bottomVector, BackRotaryEncoderGear.Shaft.GEAR_POSITION)
      )
   }

   val leftOuterRotaryEncoderKnob: LeftOuterRotaryEncoderKnob get() {
      val alphanumericPlate = alphanumericPlate

      val leftmostColumn = alphanumericPlate.columns.first()
      val keySwitch = leftmostColumn.keySwitches[LEFT_ROTARY_ENCODER_ROW_INDEX]

      val keycapTop = keySwitch.referencePoint
         .translate(keySwitch.topVector, KeySwitch.KEYCAP_SURFACE_HEIGHT)

      val tangencyPoint = alphanumericPlate.leftmostPlane intersection
            Line3d(keycapTop, keySwitch.leftVector)

      return LeftOuterRotaryEncoderKnob(
            -alphanumericPlate.leftmostPlane.normalVector
                  vectorProduct this@Case.bottomVector,
            this@Case.bottomVector,
            tangencyPoint
         )
         .let { it.translate(it.leftVector, LeftOuterRotaryEncoderKnob.RADIUS + 2.mm) }
         .let { it.translate(it.backVector, keyPitch.y / 3) }
         .let { it.translate(it.bottomVector, LeftOuterRotaryEncoderKnob.HEIGHT) }
   }

   val leftInnerRotaryEncoderKnob: LeftInnerRotaryEncoderKnob get() {
      val outerKnob = leftOuterRotaryEncoderKnob
      return LeftInnerRotaryEncoderKnob(
         outerKnob.frontVector,
         outerKnob.bottomVector,
         outerKnob.referencePoint
            .translate(outerKnob.topVector, LeftOuterRotaryEncoderKnob.HEIGHT)
      )
   }

   val frontRotaryEncoderKey: FrontRotaryEncoderKey get() {
      val knob = frontRotaryEncoderKnob
      return FrontRotaryEncoderKey(
         knob.frontVector .rotate(knob.topVector, 145.deg),
         knob.bottomVector.rotate(knob.topVector, 145.deg),
         knob.referencePoint.translate(knob.bottomVector, FRONT_ROTARY_ENCODER_KEY_Z_OFFSET)
      )
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = Case(frontVector, bottomVector, referencePoint)
}
