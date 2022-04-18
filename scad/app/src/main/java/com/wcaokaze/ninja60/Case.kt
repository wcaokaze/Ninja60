package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
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
      val ALPHANUMERIC_FRONT_RIGHT_MARGIN = 11.mm

      val FRONT_ROTARY_ENCODER_KEY_CASE_HEIGHT = 17.mm
   }

   /** [Transformable.referencePoint]を通る[axis]向きの直線を軸として回転する */
   private fun <T : Transformable<T>>
         T.rotate(axis: (T) -> Vector3d, angle: Angle): T
   {
      return rotate(
         Line3d(referencePoint, axis(this)),
         angle
      )
   }

   private fun <T : Transformable<T>>
         T.translate(direction: (T) -> Vector3d, distance: Size): T
   {
      return translate(
         direction(this),
         distance
      )
   }

   val alphanumericPlate: AlphanumericPlate get() {
      return AlphanumericPlate(frontVector, bottomVector, referencePoint)
         .rotate(AlphanumericPlate::frontVector, 15.deg)
         .translate(backVector, 69.mm)
         .translate(topVector, 85.mm)
   }

   val thumbHomeKey: KeySwitch get() {
      return KeySwitch(referencePoint, bottomVector, frontVector)
         .rotate(KeySwitch::backVector, 69.deg)
         .rotate(KeySwitch::leftVector, 1.deg)
         .rotate(KeySwitch::bottomVector, 10.deg)
         .translate(rightVector, 40.mm)
         .translate(backVector, 18.mm)
         .translate(topVector, 49.mm)
   }

   val thumbPlate: ThumbPlate get() {
      val leftmostKey = thumbHomeKey
         .translate(
            rightVector,
            distance = THUMB_KEY_PITCH * thumbHomeKey.layoutSize.x
         )
         .let { key ->
            key.rotate(
               Line3d(
                  key.referencePoint
                     .translate(key.topVector, KeySwitch.TOP_HEIGHT + KeySwitch.STEM_HEIGHT + Keycap.THICKNESS)
                     .translate(key.rightVector, THUMB_KEY_PITCH * key.layoutSize.x / 2),
                  key.frontVector
               ),
               80.deg
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

fun ScadParentObject.case(case: Case): ScadObject {
   var scad: ScadObject

   val baseCase = memoize {
      union {
         alphanumericCase(case, otherOffsets = 1.5.mm)
         //thumbCase(case, offsets = 1.5.mm)

         frontRotaryEncoderKnobCase(
            case,
            radiusOffset = PrinterAdjustments.minWallThickness.value
         )

         frontRotaryEncoderKeyCase(
            case.frontRotaryEncoderKey,
            height = Case.FRONT_ROTARY_ENCODER_KEY_CASE_HEIGHT
                  + PrinterAdjustments.minWallThickness.value,
            offset = PrinterAdjustments.minWallThickness.value
         )
      }
   }

   scad = baseCase()

   scad -= union {
      alphanumericCase(case, bottomOffset = 1.5.mm)
      //thumbCase(case)
      frontRotaryEncoderKnobCase(case)
      frontRotaryEncoderKeyCase(case.frontRotaryEncoderKey,
         height = Case.FRONT_ROTARY_ENCODER_KEY_CASE_HEIGHT)
   }


   // ==== alphanumericのプレート部 ============================================

   // キースイッチの底の高さでhull。alphanumericCaseに確実に引っ付けるために
   // 前後左右広めに生成してalphanumericCaseとのintersectionをとります
   scad += intersection {
      baseCase()
      hullAlphanumericPlate(
         case.alphanumericPlate,
         HullAlphanumericConfig(
            layerOffset = KeySwitch.BOTTOM_HEIGHT,
            frontBackOffset = 20.mm,
            leftRightOffset = 20.mm,
            columnOffset = 1.mm
         )
      )
   }

   val alphanumericHollow = memoize {
      union {
         // プレートの表面の高さでhull
         hullAlphanumericPlate(case.alphanumericPlate, HullAlphanumericConfig())

         // キーキャップの底の高さ(プレートの表面からキースイッチのストローク分
         // 高い位置)で前後左右広めにhull。こうすることでスイッチを押し込んでないときの
         // 高さとスイッチの押し込んだときの高さの二段の形状になっておしゃれです
         hullAlphanumericPlate(
            case.alphanumericPlate,
            HullAlphanumericConfig(
               layerOffset = -KeySwitch.TRAVEL,
               frontBackOffset = 20.mm,
               leftRightOffset = 20.mm
            )
         )
      }
   }

   scad -= alphanumericHollow()


   // ==== thumbのプレート部 ===================================================

   scad += thumbPlateCase(case.thumbPlate,
      height = KeySwitch.TRAVEL,
      bottomOffset = KeySwitch.BOTTOM_HEIGHT,
      otherOffsets = 1.5.mm)
   scad -= thumbPlateCase(case.thumbPlate, height = 100.mm)
   /*
   // alphanumericと同じ手法でやりましょうね
   scad += intersection {
      baseCase()
      hullThumbPlate(
         case.thumbPlate,
         layerOffset = KeySwitch.BOTTOM_HEIGHT,
         leftRightOffset = 20.mm,
         frontOffset = 20.mm
      )
   }

   val thumbHollow = memoize {
      union {
         hullThumbPlate(case.thumbPlate)

         hullThumbPlate(case.thumbPlate,
            layerOffset = -KeySwitch.TRAVEL,
            leftRightOffset = 20.mm,
            frontOffset = 20.mm
         )
      }
   }

   scad -= thumbHollow()
   */


   // ==== 奥側ロータリーエンコーダ ============================================

   /*
   scad += (
      backRotaryEncoderCase(case, otherOffsets = 1.5.mm)
      - backRotaryEncoderCase(case, bottomOffset = 1.5.mm, frontOffset = (-1.5).mm)
      - baseCase()
      + backRotaryEncoderKnobHolder(case)
      - backRotaryEncoderKnobCave(case)
   )

   scad -= backRotaryEncoderInsertionHole(case)
   scad += backRotaryEncoderMountPlate(case)
   */


   // ==== 手前側ロータリーエンコーダ ==========================================

   scad += (
      union {
         frontRotaryEncoderKnobHole(case.frontRotaryEncoderKnob,
            bottomOffset = PrinterAdjustments.minWallThickness.value,
            radiusOffset = PrinterAdjustments.minWallThickness.value)
         frontRotaryEncoderHole(case.frontRotaryEncoderKnob.rotaryEncoder,
            bottomOffset = 1.6.mm, otherOffsets = 1.5.mm)
         frontRotaryEncoderKeyHole(case.frontRotaryEncoderKey,
            height = KeySwitch.TRAVEL,
            bottomOffset = KeySwitch.BOTTOM_HEIGHT,
            innerRadiusOffset = PrinterAdjustments.minWallThickness.value,
            otherOffsets = PrinterAdjustments.minWallThickness.value)
      }
      intersection hugeCube(
         topPlane = alphanumericTopPlaneLeft(
            case.alphanumericPlate,
            offset = PrinterAdjustments.minWallThickness.value
                  + PrinterAdjustments.minHollowSize.value.z
         )
      )

      // 手前側ロータリーエンコーダは意図的にalphanumericとカブる位置に配置されてます
      // alphanumeric部分にはみ出た分を削ります
      - alphanumericHollow()
   )

   scad -= union {
      frontRotaryEncoderKnobHole(case.frontRotaryEncoderKnob)
      frontRotaryEncoderHole(case.frontRotaryEncoderKnob.rotaryEncoder)
      frontRotaryEncoderKeyHole(case.frontRotaryEncoderKey, height = 100.mm)
      frontRotaryEncoderKeyHole(
         case.frontRotaryEncoderKey,
         height = 100.mm,
         bottomOffset = frontRotaryEncoderKnobHoleZOffset()
               - FrontRotaryEncoderKey.Z_OFFSET_FROM_KNOB,
         innerRadiusOffset = 3.mm)
      rotaryEncoderMountHole(case.frontRotaryEncoderKnob.rotaryEncoder, 2.mm)
   }

   // alphanumericの壁、ノブが配置されてる列をごっそり削ります
   scad -= intersection {
      frontRotaryEncoderKnobHole(case.frontRotaryEncoderKnob, radiusOffset = 100.mm)

      hullColumn(
         case.alphanumericPlate.columns[FrontRotaryEncoderKnob.COLUMN_INDEX],
         case.alphanumericPlate.columns.getOrNull(FrontRotaryEncoderKnob.COLUMN_INDEX - 1),
         case.alphanumericPlate.columns.getOrNull(FrontRotaryEncoderKnob.COLUMN_INDEX + 1),
         HullAlphanumericConfig(
            layerOffset = 20.mm,
            frontBackOffset = 40.mm,
            columnOffset = 1.mm
         )
      )
   }


   // ==== スイッチ穴 ==========================================================

   val allSwitches = case.alphanumericPlate.columns.flatMap { it.keySwitches } +
         case.thumbHomeKey +
         case.thumbPlate.keySwitches + case.frontRotaryEncoderKey.switch

   val switchHole = memoize { switchHole() }
   val switchSideHolder = memoize { switchSideHolder() }

   scad -= union {
      for (s in allSwitches) {
         place(s) { switchHole() }
      }
   }

   scad += union {
      for (s in allSwitches) {
         place(s) { switchSideHolder() }
      }
   }

   return scad
}

// =============================================================================

private fun ScadParentObject.hugeCube(
   leftPlane:   Plane3d = Plane3d.YZ_PLANE.translate(x = (-200).mm),
   rightPlane:  Plane3d = Plane3d.YZ_PLANE.translate(x =   200 .mm),
   frontPlane:  Plane3d = Plane3d.ZX_PLANE.translate(y = (-200).mm),
   backPlane:   Plane3d = Plane3d.ZX_PLANE.translate(y =   200 .mm),
   bottomPlane: Plane3d = Plane3d.XY_PLANE.translate(z = (-200).mm),
   topPlane:    Plane3d = Plane3d.XY_PLANE.translate(z =   200 .mm)
): ScadObject {
   return distortedCube(
      topPlane, leftPlane, backPlane, rightPlane, frontPlane, bottomPlane)
}

private fun ScadParentObject.distortedCube(
   topPlane: Plane3d,
   leftPlane: Plane3d,
   backPlane: Plane3d,
   rightPlane: Plane3d,
   frontPlane: Plane3d,
   bottomPlane: Plane3d
): ScadObject {
   return hullPoints(
      listOf(leftPlane, rightPlane).flatMap { x ->
         listOf(frontPlane, backPlane).flatMap { y ->
            listOf(bottomPlane, topPlane).map { z ->
               x intersection y intersection z
            }
         }
      }
   )
}

// =============================================================================

fun ScadParentObject.alphanumericCase(
   case: Case,
   bottomOffset: Size = 0.mm,
   otherOffsets: Size = 0.mm
): ScadObject {
   val xLines = listOf(
      case.lrLine.translate(y = (-300).mm, z = (-300).mm),
      case.lrLine.translate(y = (-300).mm, z =   300 .mm),
      case.lrLine.translate(y =   300 .mm, z = (-300).mm),
      case.lrLine.translate(y =   300 .mm, z =   300 .mm),
   )

   val leftPlane  = Plane3d.YZ_PLANE.translate((-300).mm)
   val rightPlane = Plane3d.YZ_PLANE.translate(  300 .mm)

   val knobColumn = case.alphanumericPlate.columns[FrontRotaryEncoderKnob.COLUMN_INDEX]
   val knobPlane = Plane3d(knobColumn.referencePoint, knobColumn.rightVector)

   return union {
      intersection {
         hullPoints(
            listOf(leftPlane, knobPlane).flatMap { leftRightPlane ->
               xLines.map { line -> leftRightPlane intersection line }
            }
         )

         hullPoints(
            listOf(
               alphanumericLeftPlane(case.alphanumericPlate, otherOffsets),
               alphanumericRightPlane(case.alphanumericPlate, otherOffsets)
            ).flatMap { leftRightPlane ->
               listOf(
                     alphanumericBottomPlane(case, bottomOffset),
                     alphanumericBackPlane(case, otherOffsets),
                     alphanumericBackSlopePlane(case.alphanumericPlate, otherOffsets),
                     alphanumericTopPlaneLeft(case.alphanumericPlate, otherOffsets),
                     alphanumericFrontSlopePlane(case.alphanumericPlate, otherOffsets),
                     alphanumericFrontPlaneLeft(case.alphanumericPlate, otherOffsets),
                     alphanumericBottomPlane(case, bottomOffset)
                  )
                  .zipWithNext()
                  .map { (a, b) ->
                     a intersection b intersection leftRightPlane
                  }
            }
         )
      }

      intersection {
         hullPoints(
            listOf(knobPlane, rightPlane).flatMap { leftRightPlane ->
               xLines.map { line -> leftRightPlane intersection line }
            }
         )

         hullPoints(
            listOf(
               alphanumericLeftPlane(case.alphanumericPlate, otherOffsets),
               alphanumericRightPlane(case.alphanumericPlate, otherOffsets)
            ).flatMap { leftRightPlane ->
               listOf(
                     alphanumericBottomPlane(case, bottomOffset),
                     alphanumericBackPlane(case, otherOffsets),
                     alphanumericBackSlopePlane(case.alphanumericPlate, otherOffsets),
                     alphanumericTopPlaneRight(case.alphanumericPlate, otherOffsets),
                     alphanumericFrontPlaneRight(case.alphanumericPlate, otherOffsets),
                     alphanumericBottomPlane(case, bottomOffset)
                  )
                  .zipWithNext()
                  .map { (a, b) ->
                     a intersection b intersection leftRightPlane
                  }
            }
         )
      }
   }
}

fun alphanumericTopPlaneLeft(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d {
   return alphanumericTopPlane(
      alphanumericPlate,
      alphanumericPlate.columns.subList(0, FrontRotaryEncoderKnob.COLUMN_INDEX),
      offset
   )
}

fun alphanumericTopPlaneRight(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d {
   return alphanumericTopPlane(
      alphanumericPlate,
      alphanumericPlate.columns.subList(
         FrontRotaryEncoderKnob.COLUMN_INDEX, alphanumericPlate.columns.size),
      offset
   )
}

private fun alphanumericTopPlane(
   alphanumericPlate: AlphanumericPlate,
   columns: List<AlphanumericColumn>,
   offset: Size
): Plane3d {
   // 上面の平面を算出する。
   // 各Columnの一番手前の点から2点を選び、
   // その2点を通る平面が他のすべての点より上にあるとき使える

   val points = columns
      .flatMap { column ->
         val plate = column.keySwitches.last().plate(AlphanumericPlate.KEY_PLATE_SIZE)
         listOf(plate.frontLeft, plate.frontRight)
      }

   return points.asSequence()
      .iterateAllCombination()
      .filter { it.vectorAB isSameDirection alphanumericPlate.rightVector }
      .map {
         object {
            val leftPoint = it.pointA
            val rightPoint = it.pointB
            val otherPoints = it.otherPoints

            val plane = run {
               val frontVectorAverage = alphanumericPlate.columns
                  .map { c -> c.frontVector }
                  .sum()

               Plane3d(it.pointA, frontVectorAverage vectorProduct it.vectorAB)
            }
         }
      }
      .filter {
         it.otherPoints.all { p -> it.plane > p }
      }
      .minByOrNull {
         alphanumericPlate.rightVector angleWith Vector3d(it.leftPoint, it.rightPoint)
      } !!
      .plane
      .let {
         it.translate(it.normalVector, offset)
      }
}

fun alphanumericBottomPlane(case: Case, offset: Size): Plane3d
      = Plane3d(case.referencePoint, case.topVector)
      .translate(case.bottomVector, offset)

fun alphanumericLeftPlane(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d
      = alphanumericPlate.leftmostPlane.let { it.translate(it.normalVector, -offset) }
fun alphanumericRightPlane(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d
      = alphanumericPlate.rightmostPlane.let { it.translate(it.normalVector, offset) }

fun alphanumericBackSlopePlane(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d {
   // 後ろの斜めになっている部分の平面を算出する。
   // だいたいはalphanumericTopPlaneと同じ方法

   val mostBackKeyPlates = alphanumericPlate.columns
      .map { it.keySwitches.first().plate(AlphanumericPlate.KEY_PLATE_SIZE) }

   // 斜め部分のベクトル。法線ベクトルではなく斜めの方向そのもの。
   val slopeVector = mostBackKeyPlates
      .map { it.topVector }
      .maxByOrNull { it angleWith alphanumericPlate.topVector } !!

   val points = mostBackKeyPlates.flatMap { listOf(it.backLeft, it.backRight) }

   return points.asSequence()
      .iterateAllCombination()
      .filter { it.vectorAB isSameDirection alphanumericPlate.rightVector }
      .map {
         object {
            val otherPoints = it.otherPoints
            val plane = Plane3d(it.pointA, slopeVector vectorProduct it.vectorAB)
         }
      }
      .filter {
         it.otherPoints.all { p -> it.plane > p }
      }
      .minByOrNull {
         // ケースの角とキースイッチの間の角度の合計が一番小さいやつを選びます

         val slopePlane = it.plane

         mostBackKeyPlates
            .map { mostBackKey ->
               val mostBackKeyPlane = Plane3d(mostBackKey.referencePoint, mostBackKey.topVector)
               val caseCornerLine = slopePlane intersection mostBackKeyPlane

               caseCornerLine.vector angleWith mostBackKey.rightVector
            }
            .sumOf { a -> a.numberAsRadian }
      } !!
      .plane
      .let {
         it.translate(it.normalVector, offset)
      }
}

fun alphanumericBackPlane(case: Case, offset: Size): Plane3d {
   val mostBackPoint = case.alphanumericPlate.columns
      .map { it.keySwitches.first().plate(AlphanumericPlate.KEY_PLATE_SIZE) }
      .flatMap { mostBackKeyPlate ->
         listOf(mostBackKeyPlate.backLeft, mostBackKeyPlate.backRight).map {
            it.translate(mostBackKeyPlate.backVector, 1.5.mm)
               .translate(mostBackKeyPlate.bottomVector, 12.mm)
         }
      }
      .maxWithOrNull(PointOnVectorComparator(case.backVector))!!

   return Plane3d(mostBackPoint, case.backVector)
      .translate(case.backVector, offset)
}

fun alphanumericFrontSlopePlane(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d {
   val mostFrontKeyPlates = alphanumericPlate.columns
      .map { it.keySwitches.last().plate(AlphanumericPlate.KEY_PLATE_SIZE) }
      .map { it.translate(it.bottomVector, KeySwitch.BOTTOM_HEIGHT) }

   val points = mostFrontKeyPlates.flatMap { listOf(it.frontLeft, it.frontRight) }

   val rightVector = mostFrontKeyPlates.map { it.rightVector } .sum()
   val topVector = mostFrontKeyPlates.map { it.topVector } .sum()

   return points.asSequence()
      .iterateAllCombination()
      .filter { it.vectorAB isSameDirection rightVector }
      .map {
         Plane3d(it.pointA, it.vectorAB vectorProduct topVector.rotate(rightVector, 45.deg))
      }
      .minByOrNull { plane ->
         // 各KeyPlateとの角度の合計が一番小さいやつ
         mostFrontKeyPlates
            .map { mostFrontKey ->
               plane.normalVector angleWith mostFrontKey.bottomVector
            }
            .sumOf { it.numberAsRadian }
      } !!
      .let { plane ->
         // pointsのうち一番手前の点を通る平面にする
         val mostFrontPoint = points.maxWithOrNull { a, b ->
            val aPlane = Plane3d(a, plane.normalVector)
            val bPlane = Plane3d(b, plane.normalVector)
            aPlane.compareTo(bPlane)
         } !!
         Plane3d(mostFrontPoint, plane.normalVector)
      }
      .let {
         it.translate(it.normalVector, offset)
      }
}

fun alphanumericFrontPlaneLeft(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d {
   return alphanumericFrontPlane(
      alphanumericPlate,
      alphanumericPlate.columns.subList(0, FrontRotaryEncoderKnob.COLUMN_INDEX),
      offset + Case.ALPHANUMERIC_FRONT_LEFT_MARGIN
   )
}

fun alphanumericFrontPlaneRight(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d {
   return alphanumericFrontPlane(
      alphanumericPlate,
      alphanumericPlate.columns.subList(
         FrontRotaryEncoderKnob.COLUMN_INDEX, alphanumericPlate.columns.size),
      offset + Case.ALPHANUMERIC_FRONT_RIGHT_MARGIN
   )
}

private fun alphanumericFrontPlane(
   alphanumericPlate: AlphanumericPlate,
   columns: List<AlphanumericColumn>,
   offset: Size
): Plane3d {
   val mostFrontKeyPlates = columns
      .map { it.keySwitches.last().plate(AlphanumericPlate.KEY_PLATE_SIZE) }
      .map { it.translate(it.bottomVector, KeySwitch.BOTTOM_HEIGHT) }

   val points = mostFrontKeyPlates.flatMap { listOf(it.frontLeft, it.frontRight) }

   return points.asSequence()
      .iterateAllCombination()
      .filter { it.vectorAB isSameDirection alphanumericPlate.rightVector }
      .map {
         Plane3d(it.pointA, it.vectorAB vectorProduct Vector3d.Z_UNIT_VECTOR)
      }
      .minByOrNull { plane ->
         // 各KeyPlateとの角度の合計が一番小さいやつ
         mostFrontKeyPlates
            .map { mostFrontKey ->
               plane.normalVector angleWith mostFrontKey.bottomVector
            }
            .sumOf { it.numberAsRadian }
      } !!
      .let { plane ->
         // pointsのうち一番手前の点を通る平面にする
         val mostFrontPoint = points.maxWithOrNull { a, b ->
            val aPlane = Plane3d(a, plane.normalVector)
            val bPlane = Plane3d(b, plane.normalVector)
            aPlane.compareTo(bPlane)
         } !!
         Plane3d(mostFrontPoint, plane.normalVector)
      }
      .let {
         it.translate(it.normalVector, offset)
      }
}

// =============================================================================

/*
private fun ScadParentObject.thumbCase(
   case: Case,
   offsets: Size = 0.mm
): ScadObject {
   return hullThumbPlate(
      case.thumbPlate,
      layerOffset = KeySwitch.HEIGHT + offsets,
      leftRightOffset = 2.mm + offsets,
      frontOffset = offsets
   )
}
*/

private fun ScadParentObject.thumbPlateCase(
   thumbPlate: ThumbPlate,
   height: Size,
   bottomOffset: Size = 0.mm,
   otherOffsets: Size = 0.mm
): ScadObject {
   val keyLength = thumbPlate.keySwitches.maxOf { thumbPlate.keyPitch * it.layoutSize.y }
   val outerRadius = thumbPlate.layoutRadius + keyLength / 2
   val innerRadius = thumbPlate.layoutRadius - keyLength / 2

   val backAngle = Angle.PI / 2

   val startAngle = backAngle - thumbPlate.keyAngle * (thumbPlate.keySwitches.size - 0.5)
   val endAngle = backAngle + thumbPlate.keyAngle / 2

   return place(thumbPlate) {
      translate(y = -thumbPlate.layoutRadius, z = -bottomOffset) {
         difference {
            arcCylinder(radius = outerRadius + otherOffsets, height + bottomOffset,
               startAngle, endAngle, otherOffsets)
            arcCylinder(radius = innerRadius - otherOffsets, height + bottomOffset,
               startAngle, endAngle, otherOffsets)
         }
      }
   }
}

// =============================================================================

/** 平面が指定した点を通るように移動します */
private fun Plane3d.translate(point: Point3d) = Plane3d(point, normalVector)

/** 歯車に接するように移動します */
private fun Plane3d.translateTangential(gear: Gear): Plane3d {
   // 円の接点から中心へ引いた直線は必ず接線と垂直になる性質を利用すれば瞬殺で出ます
   return Plane3d(
      gear.referencePoint.translate(normalVector, gear.addendumRadius),
      normalVector
   )
}

/*
fun backRotaryEncoderCaseTopPlane(
   case: Case,
   offset: Size
): Plane3d {
   val gear = case.backRotaryEncoderGear.spurGear

   return Plane3d(
         gear.referencePoint,
         gear.bottomVector vectorProduct case.backVector
      )
      .translateTangential(gear)
      .let { it.translate(it.normalVector, offset + 0.2.mm) }
}

fun backRotaryEncoderCaseBottomPlane(case: Case, offset: Size): Plane3d
      = alphanumericBottomPlane(case, offset)

fun backRotaryEncoderCaseLeftPlane(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d {
   val column = alphanumericPlate.columns[BackRotaryEncoderKnob.COLUMN_INDEX]

   return Plane3d(column.referencePoint, column.leftVector)
      .translate(column.leftVector, BackRotaryEncoderMediationGear.CASE_WIDTH / 2 + offset)
}

fun backRotaryEncoderCaseRightPlane(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d {
   val column = alphanumericPlate.columns[BackRotaryEncoderKnob.COLUMN_INDEX]

   return Plane3d(column.referencePoint, column.rightVector)
      .translate(column.rightVector, BackRotaryEncoderMediationGear.CASE_WIDTH / 2 + offset)
}

fun backRotaryEncoderCaseSlopePlane(
   alphanumericPlate: AlphanumericPlate,
   gear: Gear,
   offset: Size
): Plane3d {
   return alphanumericBackSlopePlane(alphanumericPlate, 1.5.mm)
      .translateTangential(gear)
      .let { it.translate(it.normalVector, offset + 0.2.mm) }
}

fun backRotaryEncoderCaseFrontPlane(case: Case, offset: Size): Plane3d {
   return Plane3d(
         case.backRotaryEncoderGear.referencePoint,
         -alphanumericBackPlane(case, 0.0.mm).normalVector
      )
      .translateTangential(case.backRotaryEncoderGear.gear)
      .let { it.translate(-it.normalVector, offset + 0.2.mm) }
}

fun backRotaryEncoderCaseBackPlane(
   case: Case,
   offset: Size
): Plane3d {
   return alphanumericBackPlane(case, 0.mm)
      .translateTangential(case.backRotaryEncoderGear.gear)
      .let { it.translate(it.normalVector, offset + 0.2.mm) }
}

fun ScadParentObject.backRotaryEncoderCase(
   case: Case,
   bottomOffset: Size = 0.mm,
   frontOffset: Size = 0.mm,
   otherOffsets: Size = 0.mm
): ScadObject {
   val caseLeftPlane  = backRotaryEncoderCaseLeftPlane (case.alphanumericPlate, otherOffsets)
   val caseRightPlane = backRotaryEncoderCaseRightPlane(case.alphanumericPlate, otherOffsets)

   val caseBackPlane = backRotaryEncoderCaseBackPlane(case, otherOffsets)
   val caseBackSlopePlane = backRotaryEncoderCaseSlopePlane(case.alphanumericPlate, case.backRotaryEncoderGear.gear, otherOffsets)
   val caseTopPlane = backRotaryEncoderCaseTopPlane(case, otherOffsets)
   val caseFrontPlane = backRotaryEncoderCaseFrontPlane(case, frontOffset)
   val caseBottomPlane = backRotaryEncoderCaseBottomPlane(case, bottomOffset)

   return hullPoints(
      listOf(caseLeftPlane, caseRightPlane).flatMap { a ->
         listOf(
               caseBackPlane, caseBackSlopePlane, caseTopPlane, caseFrontPlane,
               caseBottomPlane, caseBackPlane
            )
            .zipWithNext()
            .map { (b, c) ->
               a intersection b intersection c
            }
      }
   )
}

fun ScadParentObject.backRotaryEncoderInsertionHole(case: Case): ScadObject {
   return backRotaryEncoderCase(case, frontOffset = 2.mm)
}

fun ScadParentObject.backRotaryEncoderMountPlate(case: Case): ScadObject {
   return difference {
      val rotaryEncoder = case.backRotaryEncoderGear.rotaryEncoder

      intersection {
         backRotaryEncoderCase(case, otherOffsets = 1.5.mm)

         cube(Cube(
            rotaryEncoder.referencePoint
               .translate(rotaryEncoder.leftVector,   10.0.mm)
               .translate(rotaryEncoder.frontVector,  50.0.mm)
               .translate(rotaryEncoder.bottomVector,  1.6.mm),
            Size3d(30.mm, 100.mm, 1.6.mm),
            rotaryEncoder.frontVector,
            rotaryEncoder.bottomVector
         ))
      }

      rotaryEncoderMountHole(rotaryEncoder, 2.mm)
      hullAlphanumericPlate(case.alphanumericPlate, HullAlphanumericConfig())
   }
}

fun ScadParentObject.backRotaryEncoderKnobHolder(case: Case): ScadObject {
   val topPlane = backRotaryEncoderCaseTopPlane(case, 0.mm)
      .translate(case.backRotaryEncoderKnob.referencePoint)

   val frontPlane = Plane3d(
      case.backRotaryEncoderKnob.referencePoint,
      case.backRotaryEncoderKnob.topVector vectorProduct topPlane.normalVector
   )

   val backPlane = backRotaryEncoderCaseBackPlane(case, 0.mm)

   val rightPlane = backRotaryEncoderCaseRightPlane(case.alphanumericPlate, 0.mm)
      .translate(case.backRotaryEncoderKnob.referencePoint)
      .translate(case.backRotaryEncoderKnob.topVector, BackRotaryEncoderKnob.HEIGHT)

   val leftPlane = backRotaryEncoderCaseLeftPlane(case.alphanumericPlate, 0.mm)
      .translate(case.backRotaryEncoderKnob.referencePoint)
      .translate(case.backRotaryEncoderKnob.bottomVector, BackRotaryEncoderKnob.GEAR_THICKNESS)

   return minkowski {
      hullPoints(
         listOf(backPlane, rightPlane, frontPlane, leftPlane, backPlane)
            .zipWithNext()
            .map { (a, b) -> a intersection b intersection topPlane }
      )

      rotate(
         -Vector3d.Z_UNIT_VECTOR angleWith case.backRotaryEncoderKnob.bottomVector,
         -Vector3d.Z_UNIT_VECTOR vectorProduct case.backRotaryEncoderKnob.bottomVector,
      ) {
         cylinder(
            height = 6.mm,
            radius = 4.mm,
            center = true,
            `$fa`
         )
      }
   } intersection distortedCube(
      backRotaryEncoderCaseSlopePlane(case.alphanumericPlate, case.backRotaryEncoderGear.gear, 1.5.mm),
      backRotaryEncoderCaseLeftPlane(case.alphanumericPlate, 100.mm),
      backRotaryEncoderCaseBackPlane(case, 1.5.mm),
      backRotaryEncoderCaseRightPlane(case.alphanumericPlate, 100.mm),
      Plane3d(case.referencePoint, case.frontVector),
      Plane3d(case.referencePoint, case.topVector)
   )
}

fun ScadParentObject.backRotaryEncoderKnobCave(case: Case): ScadObject {
   val knob = case.backRotaryEncoderKnob

   return place(knob) {
      translate(z = -BackRotaryEncoderKnob.GEAR_THICKNESS) {
         translate(z = (-0.6).mm) {
            cylinder(
               BackRotaryEncoderKnob.HEIGHT
                     + BackRotaryEncoderKnob.GEAR_THICKNESS
                     + 1.2.mm,
               BackRotaryEncoderKnob.RADIUS
                     + BackRotaryEncoderKnob.SKIDPROOF_RADIUS / 2
                     + 0.7.mm,
               `$fa`
            )
         }

         translate(z = (-1.5).mm) {
            cylinder(
               30.mm,
               BackRotaryEncoderKnob.SHAFT_HOLE_RADIUS,
               `$fa`
            )
         }
      }
   }
}
*/

// =============================================================================

private fun ScadObject.frontRotaryEncoderKnobHoleZOffset(): Size
      = RotaryEncoder.CLICK_TRAVEL + PrinterAdjustments.movableMargin.value

private fun ScadParentObject.frontRotaryEncoderKnobCase(
   case: Case,
   radiusOffset: Size = 0.mm
): ScadObject {
   return intersection {
      place(case.frontRotaryEncoderKnob) {
         translate(z = (-200).mm) {
            cylinder(
               height = 400.mm,
               radius = FrontRotaryEncoderKnob.RADIUS
                     + PrinterAdjustments.movableMargin.value
                     + radiusOffset
            )
         }
      }

      hugeCube(
         topPlane = alphanumericTopPlaneLeft(case.alphanumericPlate, offset = 0.mm),
         bottomPlane = alphanumericBottomPlane(case, offset = 0.mm)
      )
   }
}

private fun ScadParentObject.frontRotaryEncoderKnobHole(
   knob: FrontRotaryEncoderKnob,
   bottomOffset: Size = 0.mm,
   radiusOffset: Size = 0.mm
): ScadObject {
   return place(knob) {
      translate(z = -frontRotaryEncoderKnobHoleZOffset() - bottomOffset) {
         cylinder(
            FrontRotaryEncoderKnob.HEIGHT * 2,
            FrontRotaryEncoderKnob.RADIUS
                  + PrinterAdjustments.movableMargin.value
                  + radiusOffset
         )
      }
   }
}

private fun ScadParentObject.frontRotaryEncoderHole(
   rotaryEncoder: RotaryEncoder,
   bottomOffset: Size = 0.mm,
   otherOffsets: Size = 0.mm
): ScadObject {
   return cube(Cube(
      rotaryEncoder.referencePoint
         .translate(rotaryEncoder.leftVector,  RotaryEncoder.BODY_SIZE.x / 2 + otherOffsets)
         .translate(rotaryEncoder.frontVector, RotaryEncoder.BODY_SIZE.y / 2 + otherOffsets)
         .translate(rotaryEncoder.bottomVector, bottomOffset),
      Size3d(
         RotaryEncoder.BODY_SIZE.x + otherOffsets * 2,
         RotaryEncoder.BODY_SIZE.y + otherOffsets * 2,
         RotaryEncoder.HEIGHT + bottomOffset
      ),
      rotaryEncoder.frontVector,
      rotaryEncoder.bottomVector
   ))
}

fun ScadParentObject.frontRotaryEncoderKeyCase(
   key: FrontRotaryEncoderKey,
   height: Size,
   offset: Size = 0.mm
): ScadObject {
   return place(key) {
      translate(z = -height) {
         difference {
            val radius = FrontRotaryEncoderKey.RADIUS + FrontRotaryEncoderKey.KEY_WIDTH / 2

            val frontAngle = -Angle.PI / 2

            val startAngle = frontAngle - FrontRotaryEncoderKey.ARC_ANGLE / 2
            val endAngle   = frontAngle + FrontRotaryEncoderKey.ARC_ANGLE / 2

            arcCylinder(radius + offset, height, startAngle, endAngle, offset)
         }
      }
   }
}

fun ScadParentObject.frontRotaryEncoderKeyHole(
   key: FrontRotaryEncoderKey,
   height: Size,
   bottomOffset: Size = 0.mm,
   innerRadiusOffset: Size = 0.mm,
   otherOffsets: Size = 0.mm
): ScadObject {
   return place(key) {
      translate(z = -bottomOffset) {
         difference {
            val outerRadius = FrontRotaryEncoderKey.RADIUS + FrontRotaryEncoderKey.KEY_WIDTH / 2
            val innerRadius = FrontRotaryEncoderKey.RADIUS - FrontRotaryEncoderKey.KEY_WIDTH / 2

            val frontAngle = -Angle.PI / 2

            val startAngle = frontAngle - FrontRotaryEncoderKey.ARC_ANGLE / 2
            val endAngle   = frontAngle + FrontRotaryEncoderKey.ARC_ANGLE / 2

            arcCylinder(radius = outerRadius + otherOffsets, height + bottomOffset,
               startAngle, endAngle, otherOffsets)

            arcCylinder(radius = innerRadius - innerRadiusOffset, height + bottomOffset,
               startAngle, endAngle, otherOffsets)
         }
      }
   }
}

private infix fun Vector3d.isSameDirection(another: Vector3d): Boolean
      = this angleWith another in (-90).deg..90.deg

private class PointCombination(
   val pointA: Point3d,
   val pointB: Point3d,
   val otherPoints: List<Point3d>
) {
   val vectorAB get() = Vector3d(pointA, pointB)
}

private fun Sequence<Point3d>.iterateAllCombination(): Sequence<PointCombination> {
   val allPoints = this
   return flatMap { a ->
      (allPoints - a).map { b ->
         PointCombination(
            a, b,
            otherPoints = (allPoints - a - b).toList()
         )
      }
   }
}
