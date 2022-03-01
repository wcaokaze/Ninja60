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

   val frontRotaryEncoderKnob get() = FrontRotaryEncoderKnob(alphanumericPlate)
   val backRotaryEncoderKnob get() = BackRotaryEncoderKnob(alphanumericPlate)
   val backRotaryEncoderGear get() = BackRotaryEncoderGear(alphanumericPlate, velocityRatio = 1.0)
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
      }
   }

   /*
   scad = baseCase()

   scad -= union {
      alphanumericCase(case, bottomOffset = 1.5.mm)
      //thumbCase(case)
   }
   */


   // ==== alphanumericのプレート部 ============================================

   // キースイッチの底の高さでhull。alphanumericCaseに確実に引っ付けるために
   // 前後左右広めに生成してalphanumericCaseとのintersectionをとります
   scad = intersection {
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

   scad += (
      backRotaryEncoderCase(case, otherOffsets = 1.5.mm)
      - backRotaryEncoderCase(case, bottomOffset = 1.5.mm, frontOffset = (-1.5).mm)
      - baseCase()
      + backRotaryEncoderKnobHolder(case)
      - backRotaryEncoderKnobCave(case)
   )

   scad -= backRotaryEncoderInsertionHole(case)
   scad += backRotaryEncoderMountPlate(case)


   // ==== 手前側ロータリーエンコーダ ==========================================

   scad += (
      union {
         frontRotaryEncoderKnobHole(case.frontRotaryEncoderKnob,
            bottomOffset = 1.5.mm, radiusOffset = 1.5.mm)
         frontRotaryEncoderHole(case.frontRotaryEncoderKnob.rotaryEncoder,
            bottomOffset = 1.6.mm, otherOffsets = 1.5.mm)
         frontRotaryEncoderKeyHole(case.frontRotaryEncoderKey,
            height = KeySwitch.TRAVEL,
            bottomOffset = KeySwitch.BOTTOM_HEIGHT,
            otherOffsets = 1.5.mm)
      }
      intersection distortedCube(
         topPlane = alphanumericTopPlane(case.alphanumericPlate, offset = 1.5.mm),
         bottomPlane = Plane3d.XY_PLANE.translate(z = (-100).mm),
         rightPlane  = Plane3d.YZ_PLANE.translate(x =   100 .mm),
         leftPlane   = Plane3d.YZ_PLANE.translate(x = (-100).mm),
         backPlane   = Plane3d.ZX_PLANE.translate(y =   100 .mm),
         frontPlane  = Plane3d.ZX_PLANE.translate(y = (-100).mm)
      )

      // 手前側ロータリーエンコーダは意図的にalphanumericとカブる位置に配置されてます
      // alphanumeric部分にはみ出た分を削ります
      - alphanumericHollow()
   )

   scad -= union {
      frontRotaryEncoderKnobHole(case.frontRotaryEncoderKnob)
      frontRotaryEncoderHole(case.frontRotaryEncoderKnob.rotaryEncoder)
      frontRotaryEncoderKeyHole(case.frontRotaryEncoderKey, height = 100.mm, otherOffsets = 0.1.mm)
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
            frontBackOffset = 20.mm,
            columnOffset = 1.mm
         )
      )
   }


   // ==== スイッチ穴 ==========================================================

   val allSwitches = case.alphanumericPlate.columns.flatMap { it.keySwitches } +
         case.thumbHomeKey + case.thumbPlate.keySwitches + case.frontRotaryEncoderKey.switch

   scad -= union {
      for (s in allSwitches) {
         switchHole(s)
      }
   }

   scad += union {
      for (s in allSwitches) {
         switchSideHolder(s)
      }
   }

   return scad
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
   return hullPoints(
      listOf(
         alphanumericLeftPlane(case.alphanumericPlate, otherOffsets),
         alphanumericRightPlane(case.alphanumericPlate, otherOffsets)
      ).flatMap { leftRightPlane ->
         listOf(
               alphanumericBottomPlane(case, bottomOffset),
               alphanumericBackPlane(case, otherOffsets),
               alphanumericBackSlopePlane(case.alphanumericPlate, otherOffsets),
               alphanumericTopPlane(case.alphanumericPlate, otherOffsets),
               alphanumericFrontPlane(case.thumbPlate, otherOffsets),
               alphanumericBottomPlane(case, bottomOffset)
            )
            .zipWithNext()
            .map { (a, b) ->
               a intersection b intersection leftRightPlane
            }
      }
   )
}

fun alphanumericTopPlane(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d {
   // 上面の平面を算出する。
   // 各Columnの一番手前の点から2点を選び、
   // その2点を通る平面が他のすべての点より上にあるとき使える

   val frontVector = alphanumericPlate.columns.map { it.frontVector } .sum()

   val points = alphanumericPlate.columns
      .flatMap { column ->
         val plate = column.keySwitches.last().plate(AlphanumericPlate.KEY_PLATE_SIZE)
         listOf(plate.frontLeft, plate.frontRight)
      }

   return points.flatMap { left ->
         (points - left).map { right ->
            val otherPoints = points - left - right
            Triple(left, right, otherPoints)
         }
      }
      .asSequence()
      .filter { (left, right) ->
         // 総当りなので右から左のベクトルが混入してます
         // filterします
         // もうちょっといいやり方があるといいですね
         Vector3d(
            alphanumericPlate.columns.first().referencePoint,
            alphanumericPlate.columns.last() .referencePoint
         ) angleWith Vector3d(left, right) in (-90).deg..90.deg
      }
      .map { (left, right, otherPoints) ->
         object {
            val left = left
            val right = right
            val otherPoints = otherPoints

            /** 2点を通りfrontVectorと平行な平面 */
            val plane = Plane3d(left, frontVector vectorProduct Vector3d(left, right))
         }
      }
      .filter {
         it.otherPoints.all { p ->
            /** pからplaneへの垂線 */
            val perpendicular = Line3d(p, it.plane.normalVector)
            /** 垂線とplaneの交点 */
            val intersectionPoint = it.plane intersection perpendicular

            // pから交点へのベクトルを改めて算出し、planeの法線ベクトルと比較
            // 0°もしくは180°となるはずで、0°の場合planeより下にpが存在する
            Vector3d(p, intersectionPoint) angleWith it.plane.normalVector in (-90).deg..90.deg
         }
      }
      .minByOrNull {
         Vector3d(
            alphanumericPlate.columns.first().referencePoint,
            alphanumericPlate.columns.last() .referencePoint
         ) angleWith Vector3d(it.left, it.right)
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

   val alphanumericPlateTopVector = alphanumericPlate.columns.map { it.topVector } .sum()

   val slopeVector = mostBackKeyPlates
      .map { it.topVector }
      .maxByOrNull { it angleWith alphanumericPlateTopVector } !!

   val points = mostBackKeyPlates.flatMap { listOf(it.backLeft, it.backRight) }

   return points.flatMap { left ->
         (points - left).map { right ->
            val otherPoints = points - left - right
            Triple(left, right, otherPoints)
         }
      }
      .asSequence()
      .filter { (left, right) ->
         Vector3d(
            alphanumericPlate.columns.first().referencePoint,
            alphanumericPlate.columns.last() .referencePoint
         ) angleWith Vector3d(left, right) in (-90).deg..90.deg
      }
      .map { (left, right, otherPoints) ->
         object {
            val otherPoints = otherPoints

            val plane = Plane3d(left, slopeVector vectorProduct Vector3d(left, right))
         }
      }
      .filter {
         it.otherPoints.all { p ->
            val perpendicular = Line3d(p, it.plane.normalVector)
            val intersectionPoint = it.plane intersection perpendicular

            Vector3d(p, intersectionPoint) angleWith it.plane.normalVector in (-90).deg..90.deg
         }
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

fun alphanumericFrontPlane(thumbPlate: ThumbPlate, offset: Size): Plane3d {
   return Plane3d.ZX_PLANE
      .translate(Vector3d.Y_UNIT_VECTOR, 9.mm)
      .translate(Vector3d.Y_UNIT_VECTOR, -offset)
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

fun backRotaryEncoderCaseTopPlane(
   case: Case,
   offset: Size
): Plane3d {
   val gear = case.backRotaryEncoderGear.gear

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
      .translate(column.leftVector, BackRotaryEncoderGear.CASE_WIDTH / 2 + offset)
}

fun backRotaryEncoderCaseRightPlane(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d {
   val column = alphanumericPlate.columns[BackRotaryEncoderKnob.COLUMN_INDEX]

   return Plane3d(column.referencePoint, column.rightVector)
      .translate(column.rightVector, BackRotaryEncoderGear.CASE_WIDTH / 2 + offset)
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

// =============================================================================

private fun ScadParentObject.frontRotaryEncoderKnobHole(
   knob: FrontRotaryEncoderKnob,
   bottomOffset: Size = 0.mm,
   radiusOffset: Size = 0.mm
): ScadObject {
   return place(knob) {
      translate(z = (-1.5).mm - bottomOffset) {
         cylinder(
            FrontRotaryEncoderKnob.HEIGHT * 2,
            FrontRotaryEncoderKnob.RADIUS + 0.7.mm + radiusOffset,
            `$fa`
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

fun ScadParentObject.frontRotaryEncoderKeyHole(
   key: FrontRotaryEncoderKey,
   height: Size,
   bottomOffset: Size = 0.mm,
   otherOffsets: Size = 0.mm
): ScadObject {
   return place(key) {
      translate(z = -bottomOffset) {
         difference {
            val outerRadius = FrontRotaryEncoderKey.RADIUS + FrontRotaryEncoderKey.KEY_WIDTH / 2
            val innerRadius = FrontRotaryEncoderKey.RADIUS - FrontRotaryEncoderKey.KEY_WIDTH / 2

            val frontAngle = -Angle.PI / 2
            val offsetAngle = Angle(otherOffsets / innerRadius)

            val startAngle = frontAngle - FrontRotaryEncoderKey.ARC_ANGLE / 2
            val endAngle   = frontAngle + FrontRotaryEncoderKey.ARC_ANGLE / 2

            arcCylinder(radius = outerRadius + otherOffsets, height + bottomOffset,
               startAngle - offsetAngle, endAngle + offsetAngle, `$fa`)

            arcCylinder(radius = innerRadius - otherOffsets, height + bottomOffset,
               startAngle - offsetAngle, endAngle + offsetAngle, `$fa`)
         }
      }
   }
}
