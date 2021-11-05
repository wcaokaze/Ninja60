package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

data class Case(
   val alphanumericPlate: AlphanumericPlate,
   val thumbPlate: ThumbPlate
) {
   companion object {
      operator fun invoke(): Case {
         return Case(
            AlphanumericPlate()
               .rotate(Line3d.Y_AXIS, (-15).deg)
               .translate(x = 0.mm, y = 69.mm, z = 85.mm),
            ThumbPlate()
               .rotate(Line3d.Y_AXIS, 69.deg)
               .rotate(Line3d.X_AXIS, (-7).deg)
               .rotate(Line3d.Z_AXIS, (-8).deg)
               .translate(x = 66.mm, y = 0.mm, z = 53.mm)
         )
      }
   }

   val frontRotaryEncoderKnob get() = FrontRotaryEncoderKnob(alphanumericPlate)
   val backRotaryEncoderKnob get() = BackRotaryEncoderKnob(alphanumericPlate)
   val backRotaryEncoderGear get() = BackRotaryEncoderGear(alphanumericPlate, velocityRatio = 1.0)
}

fun ScadParentObject.case(case: Case): ScadObject {
   return (
      union {
         alphanumericFrontCase(case.alphanumericPlate)
         thumbCase(case.thumbPlate)
      }
      - union {
         alphanumericTopCave(case.alphanumericPlate)
         thumbCave(case.thumbPlate)
      }

      + backRotaryEncoderCase(case)
      + backRotaryEncoderKnobHolder(case)
      - backRotaryEncoderCave(case)
      - backRotaryEncoderKnobCave(case)

      - hullAlphanumericPlate(case.alphanumericPlate)
      - alphanumericBottomCave(case.alphanumericPlate)

      + difference {
         alphanumericPlate(case.alphanumericPlate)
         backRotaryEncoderInsertionHole(case)
      }

      + thumbPlate(case.thumbPlate)

      + backRotaryEncoderMountPlate(case)

      - frontRotaryEncoderHole(case.frontRotaryEncoderKnob)
      + frontRotaryEncoderMountPlate(case.frontRotaryEncoderKnob)
   )
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

fun alphanumericBottomPlane(offset: Size): Plane3d
      = Plane3d.XY_PLANE.translate(Vector3d.Z_UNIT_VECTOR, -offset)

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

fun alphanumericBackPlane(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d {
   val y = alphanumericPlate.columns
      .map { it.keySwitches.first().plate(AlphanumericPlate.KEY_PLATE_SIZE) }
      .flatMap { mostBackKeyPlate ->
         listOf(mostBackKeyPlate.backLeft, mostBackKeyPlate.backRight).map {
            it.translate(mostBackKeyPlate.backVector, 1.5.mm)
               .translate(mostBackKeyPlate.bottomVector, 12.mm)
         }
      }
      .maxOf { it.y }

   return Plane3d.ZX_PLANE
      .translate(Vector3d.Y_UNIT_VECTOR, y.distanceFromOrigin)
      .translate(Vector3d.Y_UNIT_VECTOR, offset)
}

fun alphanumericFrontPlane(offset: Size): Plane3d {
   return Plane3d.ZX_PLANE
      .translate(Vector3d.Y_UNIT_VECTOR, 9.mm)
      .translate(Vector3d.Y_UNIT_VECTOR, -offset)
}

private fun ScadParentObject.alphanumericFrontCase(alphanumericPlate: AlphanumericPlate): ScadObject {
   val topPlane = alphanumericTopPlane(alphanumericPlate, 3.mm)
   val bottomPlane = alphanumericBottomPlane(0.mm)
   val leftPlane = alphanumericLeftPlane(alphanumericPlate, 1.5.mm)
   val rightPlane = alphanumericRightPlane(alphanumericPlate, 1.5.mm)
   val backSlopePlane = alphanumericBackSlopePlane(alphanumericPlate, 1.5.mm)
   val backPlane = alphanumericBackPlane(alphanumericPlate, 1.5.mm)
   val frontPlane = alphanumericFrontPlane(1.5.mm)

   return intersection {
      distortedCube(topPlane, leftPlane, backPlane, rightPlane, frontPlane, bottomPlane)
      distortedCube(backSlopePlane, leftPlane, backPlane, rightPlane, frontPlane, bottomPlane)
   }
}

private fun ScadParentObject.alphanumericTopCave(alphanumericPlate: AlphanumericPlate): ScadObject {
   return union {
      hullAlphanumericPlate(alphanumericPlate, layerOffset = -KeySwitch.TRAVEL, frontBackOffset = 20.mm, leftRightOffset = 20.mm)
      hullAlphanumericPlate(alphanumericPlate)
   }
}

private fun ScadParentObject.alphanumericBottomCave(alphanumericPlate: AlphanumericPlate): ScadObject {
   return intersection {
      hullAlphanumericPlate(alphanumericPlate, layerOffset = 100.mm)

      distortedCube(
         alphanumericTopPlane(alphanumericPlate, 0.mm),
         alphanumericLeftPlane(alphanumericPlate, 0.mm),
         alphanumericBackPlane(alphanumericPlate, 0.mm),
         alphanumericRightPlane(alphanumericPlate, 0.mm),
         alphanumericFrontPlane(0.mm),
         alphanumericBottomPlane(1.mm)
      )
   }
}

// =============================================================================

private fun ScadParentObject.thumbCase(plate: ThumbPlate): ScadObject {
   /*
   translate(66.mm, 0.mm, 0.mm) {
      rotate(z = (-8).deg) {
         rotate(x = (-7).deg) {
            rotate(y = 69.deg) {
               translate(y = 14.mm, z = 32.mm) {
                  cube(68.mm, 54.mm, 42.mm, center = true)
               }
            }
         }
      }
   }
   */

   return hullThumbPlate(plate, layerOffset = 12.mm, leftRightOffset = 5.mm, frontOffset = 2.mm)
}

private fun ScadParentObject.thumbCave(plate: ThumbPlate): ScadObject {
   return union {
      hullThumbPlate(plate, leftRightOffset = 20.mm, frontOffset = 20.mm)
      hullThumbPlate(plate, layerOffset = 10.mm)
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

/**
 * 法線ベクトルの向きを正としたとき、より大きい位置にある平面を返します
 * 2つの平面の法線ベクトルは同じ向きである必要があります
 */
private fun max(a: Plane3d, b: Plane3d): Plane3d {
   require(a.normalVector angleWith b.normalVector < 0.01.deg)

   val line = Line3d(Point3d.ORIGIN, a.normalVector)
   val vAB = Vector3d(a intersection line, b intersection line)

   return if (vAB angleWith a.normalVector in (-90).deg..90.deg) {
      b
   } else {
      a
   }
}

fun backRotaryEncoderCaseTopPlane(
   gear: BackRotaryEncoderGear,
   offset: Size
): Plane3d {
   return Plane3d(
         gear.gear.referencePoint,
         gear.gear.bottomVector vectorProduct Vector3d.Y_UNIT_VECTOR
      )
      .translateTangential(gear.gear)
      .let { it.translate(it.normalVector, offset + 0.2.mm) }
}

fun backRotaryEncoderCaseBottomPlane(offset: Size): Plane3d = alphanumericBottomPlane(offset)

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

fun backRotaryEncoderCaseFrontPlane(gear: BackRotaryEncoderGear, offset: Size): Plane3d {
   return Plane3d(Point3d.ORIGIN, -Vector3d.Y_UNIT_VECTOR)
      .translateTangential(gear.gear)
      .let { it.translate(it.normalVector, offset + 0.2.mm) }
}

fun backRotaryEncoderCaseBackPlane(
   alphanumericPlate: AlphanumericPlate,
   gear: Gear,
   offset: Size
): Plane3d {
   return alphanumericBackPlane(alphanumericPlate, 0.mm)
      .translateTangential(gear)
      .let { it.translate(it.normalVector, offset + 0.2.mm) }
}

fun ScadParentObject.backRotaryEncoderCase(
   alphanumericPlate: AlphanumericPlate,
   gear: BackRotaryEncoderGear,
   offset: Size
): ScadObject {
   val caseLeftPlane  = backRotaryEncoderCaseLeftPlane (alphanumericPlate, offset)
   val caseRightPlane = backRotaryEncoderCaseRightPlane(alphanumericPlate, offset)

   val caseBackPlane = backRotaryEncoderCaseBackPlane(alphanumericPlate, gear.gear, offset)
   val caseBackSlopePlane = backRotaryEncoderCaseSlopePlane(alphanumericPlate, gear.gear, offset)
   val caseTopPlane = backRotaryEncoderCaseTopPlane(gear, offset)
   val caseFrontPlane = backRotaryEncoderCaseFrontPlane(gear, offset)
   val caseBottomPlane = backRotaryEncoderCaseBottomPlane(0.mm)

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

fun ScadParentObject.backRotaryEncoderCase(case: Case): ScadObject {
   return difference {
      backRotaryEncoderCase(
         case.alphanumericPlate,
         case.backRotaryEncoderGear,
         offset = 1.5.mm
      )

      hullAlphanumericPlate(case.alphanumericPlate, layerOffset = 10.mm)
   }
}

fun ScadParentObject.backRotaryEncoderCave(case: Case): ScadObject {
   return backRotaryEncoderCase(
      case.alphanumericPlate,
      case.backRotaryEncoderGear,
      offset = 0.mm
   )
}

fun ScadParentObject.backRotaryEncoderInsertionHole(case: Case): ScadObject {
   val rotaryEncoder = case.backRotaryEncoderGear.rotaryEncoder

   return (
      intersection {
         backRotaryEncoderCase(
            case.alphanumericPlate,
            case.backRotaryEncoderGear,
            offset = 0.mm
         )

         cube(Cube(
            rotaryEncoder.referencePoint
               .translate(rotaryEncoder.leftVector,    8.mm)
               .translate(rotaryEncoder.frontVector,  10.mm)
               .translate(rotaryEncoder.bottomVector,  7.mm),
            Size3d(18.mm, 20.mm, 7.mm),
            rotaryEncoder.frontVector,
            rotaryEncoder.bottomVector
         ))
      }

      + intersection {
         backRotaryEncoderCase(
            case.alphanumericPlate,
            case.backRotaryEncoderGear,
            offset = 0.mm
         )

         cube(
            Cube(
               rotaryEncoder.referencePoint,
               Size3d(100.mm, 12.mm, 40.mm),
               backRotaryEncoderCaseBackPlane(
                  case.alphanumericPlate, case.backRotaryEncoderGear.gear, 0.mm
               ).normalVector,
               rotaryEncoder.bottomVector
            )
            .let { it.translate(it.leftVector,  50.mm) }
            .let { it.translate(it.frontVector, 6.mm) }
         )
      }

      + rotaryEncoderMountHole(rotaryEncoder, 2.mm)
   )
}

fun ScadParentObject.backRotaryEncoderMountPlate(case: Case): ScadObject {
   return difference {
      val rotaryEncoder = case.backRotaryEncoderGear.rotaryEncoder

      intersection {
         backRotaryEncoderCase(
            case.alphanumericPlate,
            case.backRotaryEncoderGear,
            offset = 1.5.mm
         )

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
      hullAlphanumericPlate(case.alphanumericPlate)
   }
}

fun ScadParentObject.backRotaryEncoderKnobHolder(case: Case): ScadObject {
   val topPlane = backRotaryEncoderCaseTopPlane(case.backRotaryEncoderGear, 0.mm)
      .translate(case.backRotaryEncoderKnob.referencePoint)

   val frontPlane = Plane3d(
      case.backRotaryEncoderKnob.referencePoint,
      case.backRotaryEncoderKnob.topVector vectorProduct topPlane.normalVector
   )

   val backPlane = backRotaryEncoderCaseBackPlane(
      case.alphanumericPlate, case.backRotaryEncoderGear.gear, 0.mm)

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
      Plane3d.YZ_PLANE.translate(x = (-100).mm),
      backRotaryEncoderCaseBackPlane(case.alphanumericPlate, case.backRotaryEncoderGear.gear, 1.5.mm),
      Plane3d.YZ_PLANE.translate(x = 100 .mm),
      Plane3d.ZX_PLANE,
      Plane3d.XY_PLANE
   )
}

fun ScadParentObject.backRotaryEncoderKnobCave(case: Case): ScadObject {
   val knob = case.backRotaryEncoderKnob

   val locale = knob.referencePoint
      .translate(knob.bottomVector, BackRotaryEncoderKnob.GEAR_THICKNESS)

   val height = BackRotaryEncoderKnob.HEIGHT + BackRotaryEncoderKnob.GEAR_THICKNESS
   val radius = BackRotaryEncoderKnob.RADIUS

   return locale(locale.translate(knob.bottomVector, 0.6.mm)) {
      rotate(
         -Vector3d.Z_UNIT_VECTOR angleWith knob.bottomVector,
         -Vector3d.Z_UNIT_VECTOR vectorProduct knob.bottomVector,
      ) {
         cylinder(
            height + 1.2.mm,
            radius + 0.6.mm,
            `$fa`
         )
      }
   }
}

// =============================================================================

private fun ScadParentObject.frontRotaryEncoderHole(knob: FrontRotaryEncoderKnob): ScadObject {
   val rotaryEncoder = knob.rotaryEncoder()

   return union {
      cube(Cube(
         rotaryEncoder.referencePoint
            .translate(rotaryEncoder.leftVector,  RotaryEncoder.BODY_SIZE.x / 2)
            .translate(rotaryEncoder.frontVector, RotaryEncoder.BODY_SIZE.y / 2)
            .translate(rotaryEncoder.bottomVector, 6.mm),
         RotaryEncoder.BODY_SIZE.copy(z = RotaryEncoder.HEIGHT + 6.mm),
         rotaryEncoder.frontVector,
         rotaryEncoder.bottomVector
      ))

      locale(knob.referencePoint) {
         rotate(
            Vector3d.Z_UNIT_VECTOR angleWith     rotaryEncoder.topVector,
            Vector3d.Z_UNIT_VECTOR vectorProduct rotaryEncoder.topVector
         ) {
            translate(z = (-1).mm) {
               cylinder(FrontRotaryEncoderKnob.HEIGHT * 2,
                  FrontRotaryEncoderKnob.RADIUS + 2.mm, `$fa`)
            }
         }
      }
   }
}

private fun ScadParentObject.frontRotaryEncoderMountPlate
      (knob: FrontRotaryEncoderKnob): ScadObject
{
   val rotaryEncoder = knob.rotaryEncoder()

   return difference {
      cube(Cube(
         rotaryEncoder.referencePoint
            .translate(rotaryEncoder.leftVector,  RotaryEncoder.BODY_SIZE.x / 2)
            .translate(rotaryEncoder.frontVector, RotaryEncoder.BODY_SIZE.y / 2)
            .translate(rotaryEncoder.bottomVector, 1.6.mm),
         RotaryEncoder.BODY_SIZE.copy(z = 1.6.mm),
         rotaryEncoder.frontVector,
         rotaryEncoder.bottomVector
      ))

      rotaryEncoderMountHole(rotaryEncoder, 2.mm)
   }
}
