package com.wcaokaze.ninja60.case

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.case.scad.*
import com.wcaokaze.ninja60.parts.key.*
import com.wcaokaze.ninja60.parts.key.alphanumeric.*
import com.wcaokaze.ninja60.parts.key.thumb.*
import com.wcaokaze.ninja60.parts.rotaryencoder.*
import com.wcaokaze.ninja60.parts.rotaryencoder.back.*
import com.wcaokaze.ninja60.parts.rotaryencoder.front.*
import com.wcaokaze.ninja60.parts.rotaryencoder.gear.*
import com.wcaokaze.ninja60.parts.rotaryencoder.left.*
import com.wcaokaze.ninja60.shared.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.ninja60.shared.scadutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

fun PropagatedValueProvider.Case() = Case(this)

class Case private constructor(
   override val referencePoint: Point3d,
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d,
   val alphanumericPlate: AlphanumericPlate,
   val thumbHomeKey: KeySwitch,
   val thumbPlate: ThumbPlate,
   val frontRotaryEncoderKnob: FrontRotaryEncoderKnob,
   val backRotaryEncoderKnob: BackRotaryEncoderKnob,
   val backRotaryEncoderMediationGear: BackRotaryEncoderMediationGear,
   val backRotaryEncoderGear: BackRotaryEncoderGear,
   val leftOuterRotaryEncoderKnob: LeftOuterRotaryEncoderKnob,
   val leftInnerRotaryEncoderKnob: LeftInnerRotaryEncoderKnob,
   val frontRotaryEncoderKey: FrontRotaryEncoderKey,
) : Transformable<Case>, Placeable<Case> {
   companion object {
      val THUMB_KEY_PITCH = 19.2.mm

      /** 何番目の[AlphanumericColumn]に[FrontRotaryEncoderKnob]を配置するか */
      const val FRONT_ROTARY_ENCODER_COLUMN_INDEX = 3
      /** 何番目の[AlphanumericColumn]に[BackRotaryEncoderKnob]を配置するか */
      const val BACK_ROTARY_ENCODER_COLUMN_INDEX = 3
      /** 奥から何番目の[KeySwitch]に[LeftOuterRotaryEncoderKnob]を配置するか */
      const val LEFT_ROTARY_ENCODER_ROW_INDEX = 2

      /**
       * [FrontRotaryEncoderKey]と[FrontRotaryEncoderKnob]の
       * [bottomVector][Placeable.bottomVector]方向のズレ。
       *
       * [Placeable.referencePoint]基準。つまりノブの表面とかキーの表面ではなく、
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
      val BACK_ROTARY_ENCODER_GEAR_HOLDER_ARM_WIDTH = 6.mm

      operator fun invoke(propagatedValueProvider: PropagatedValueProvider): Case {
         val caseReferencePoint = Point3d.ORIGIN
         val caseLeftVector   = -Vector3d.X_UNIT_VECTOR
         val caseRightVector  =  Vector3d.X_UNIT_VECTOR
         val caseFrontVector  = -Vector3d.Y_UNIT_VECTOR
         val caseBackVector   =  Vector3d.Y_UNIT_VECTOR
         val caseBottomVector = -Vector3d.Z_UNIT_VECTOR
         val caseTopVector    =  Vector3d.Z_UNIT_VECTOR

         val alphanumericPlate = run {
            AlphanumericPlate(caseFrontVector, caseBottomVector, caseReferencePoint)
               .transform { rotate(frontVectorLine, 15.deg) }
               .translate(caseBackVector, 69.mm)
               .translate(caseTopVector, 65.mm)
         }

         val thumbHomeKey = run {
            KeySwitch(caseReferencePoint, caseBottomVector, caseFrontVector)
               .transform { rotate(backVectorLine, 65.deg) }
               .transform { rotate(leftVectorLine, 1.deg) }
               .transform { rotate(bottomVectorLine, 14.deg) }
               .translate(caseRightVector, 25.mm)
               .translate(caseBackVector, 17.mm)
               .translate(caseTopVector, 29.mm)
         }

         val thumbPlate = run {
            val leftmostKey = thumbHomeKey
               .transform { translate(rightVector, THUMB_KEY_PITCH * thumbHomeKey.layoutSize.x) }
               .transform {
                  rotate(
                     frontVectorLine.translate(topVector, KeySwitch.KEYCAP_SURFACE_HEIGHT)
                                    .translate(leftVector, THUMB_KEY_PITCH * layoutSize.x / 2),
                     69.deg
                  )
               }

            ThumbPlate(
               leftmostKey.referencePoint, layoutRadius = 60.mm, THUMB_KEY_PITCH,
               leftmostKey.frontVector, leftmostKey.bottomVector
            )
         }

         val frontRotaryEncoderKnob = run {
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

            FrontRotaryEncoderKnob(
               column.frontVector vectorProduct alphanumericTopPlane.normalVector,
               -alphanumericTopPlane.normalVector,
               knobCenter
            )
         }

         val backRotaryEncoderKnob = run {
            val column = alphanumericPlate.columns[BACK_ROTARY_ENCODER_COLUMN_INDEX]
            val mostBackKey = column.keySwitches.first()
            val keyBackPoint = mostBackKey.referencePoint
               .translate(mostBackKey.backVector, AlphanumericPlate.KEY_PLATE_SIZE.y)
            val knobCenter = keyBackPoint
               .translate(mostBackKey.topVector, BackRotaryEncoderKnob.RADIUS * 2)
               .translate(mostBackKey.leftVector, BackRotaryEncoderKnob.HEIGHT / 2)
               .translate(mostBackKey.topVector, 4.mm)
               .translate(mostBackKey.backVector, 2.mm)

            BackRotaryEncoderKnob(
               mostBackKey.topVector,
               mostBackKey.leftVector,
               knobCenter,
               with (propagatedValueProvider) {
                  BackRotaryEncoderMediationGear.SpurGear.THICKNESS +
                        PrinterAdjustments.movableMargin.value
               }
            )
         }

         val backRotaryEncoderMediationGear = backRotaryEncoderKnob.let { knob ->
            val alphanumericCasePlane = alphanumericBackSlopePlane(alphanumericPlate, offset = 1.7.mm)

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
               with (propagatedValueProvider) {
                  gear.addendumRadius + PrinterAdjustments.movableMargin.value
               }
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

            BackRotaryEncoderMediationGear(gear.frontVector, gear.bottomVector, p)
         }

         val backRotaryEncoderGear = backRotaryEncoderMediationGear.let { mediationGear ->
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

            val shaftHeight: Size
            val shaftRadius: Size
            val shaftHoleDepth: Size

            with (propagatedValueProvider) {
               shaftRadius = RotaryEncoder.SHAFT_RADIUS +
                     PrinterAdjustments.minWallThickness.value
               shaftHoleDepth = RotaryEncoder.SHAFT_HEIGHT -
                     RotaryEncoder.CLICK_TRAVEL -
                     PrinterAdjustments.movableMargin.value
               shaftHeight = shaftHoleDepth +
                     PrinterAdjustments.minWallThickness.value
            }

            BackRotaryEncoderGear(
               gear.referencePoint.translate(gear.bottomVector, BackRotaryEncoderGear.Shaft.GEAR_POSITION),
               gear.frontVector,
               gear.bottomVector,
               shaftHeight,
               shaftRadius,
               shaftHoleDepth
            )
         }

         val leftOuterRotaryEncoderKnob = run {
            val leftmostColumn = alphanumericPlate.columns.first()
            val keySwitch = leftmostColumn.keySwitches[LEFT_ROTARY_ENCODER_ROW_INDEX]

            val keycapTop = keySwitch.referencePoint
               .translate(keySwitch.topVector, KeySwitch.KEYCAP_SURFACE_HEIGHT)

            val tangencyPoint = alphanumericPlate.leftmostPlane intersection
                  Line3d(keycapTop, keySwitch.leftVector)

            LeftOuterRotaryEncoderKnob(
                  -alphanumericPlate.leftmostPlane.normalVector
                        vectorProduct caseBottomVector,
                  caseBottomVector,
                  tangencyPoint
               )
               .let { it.translate(it.leftVector, LeftOuterRotaryEncoderKnob.RADIUS + 2.mm) }
               .let { it.translate(it.backVector, keyPitch.y / 3) }
               .let { it.translate(it.bottomVector, LeftOuterRotaryEncoderKnob.HEIGHT) }
         }

         val leftInnerRotaryEncoderKnob = leftOuterRotaryEncoderKnob.let { outerKnob ->
            LeftInnerRotaryEncoderKnob(
               outerKnob.frontVector,
               outerKnob.bottomVector,
               outerKnob.referencePoint
                  .translate(outerKnob.topVector, LeftOuterRotaryEncoderKnob.HEIGHT)
            )
         }

         val frontRotaryEncoderKey = frontRotaryEncoderKnob.let { knob ->
            FrontRotaryEncoderKey(
               knob.frontVector .rotate(knob.topVector, 145.deg),
               knob.bottomVector.rotate(knob.topVector, 145.deg),
               knob.referencePoint.translate(knob.bottomVector, FRONT_ROTARY_ENCODER_KEY_Z_OFFSET)
            )
         }

         return Case(
            caseReferencePoint, caseFrontVector, caseBottomVector, alphanumericPlate,
            thumbHomeKey, thumbPlate, frontRotaryEncoderKnob, backRotaryEncoderKnob,
            backRotaryEncoderMediationGear, backRotaryEncoderGear,
            leftOuterRotaryEncoderKnob, leftInnerRotaryEncoderKnob, frontRotaryEncoderKey,
         )
      }
   }

   override fun translate(distance: Size3d) = Case(
      referencePoint.translate(distance),
      frontVector,
      bottomVector,
      alphanumericPlate             .translate(distance),
      thumbHomeKey                  .translate(distance),
      thumbPlate                    .translate(distance),
      frontRotaryEncoderKnob        .translate(distance),
      backRotaryEncoderKnob         .translate(distance),
      backRotaryEncoderMediationGear.translate(distance),
      backRotaryEncoderGear         .translate(distance),
      leftOuterRotaryEncoderKnob    .translate(distance),
      leftInnerRotaryEncoderKnob    .translate(distance),
      frontRotaryEncoderKey         .translate(distance),
   )

   override fun translate(distance: Vector3d): Case
         = translate(Size3d(distance.x, distance.y, distance.z))

   override fun translate(direction: Vector3d, distance: Size): Case
         = translate(direction.toUnitVector() * distance.numberAsMilliMeter)

   override fun rotate(axis: Line3d, angle: Angle) = Case(
      referencePoint,
      frontVector .rotate(axis.vector, angle),
      bottomVector.rotate(axis.vector, angle),
      alphanumericPlate             .rotate(axis, angle),
      thumbHomeKey                  .rotate(axis, angle),
      thumbPlate                    .rotate(axis, angle),
      frontRotaryEncoderKnob        .rotate(axis, angle),
      backRotaryEncoderKnob         .rotate(axis, angle),
      backRotaryEncoderMediationGear.rotate(axis, angle),
      backRotaryEncoderGear         .rotate(axis, angle),
      leftOuterRotaryEncoderKnob    .rotate(axis, angle),
      leftInnerRotaryEncoderKnob    .rotate(axis, angle),
      frontRotaryEncoderKey         .rotate(axis, angle),
   )
}
