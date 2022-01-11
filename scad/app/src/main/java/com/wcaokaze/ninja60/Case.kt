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

   /** [Transformable.referencePoint]を通る[axis]向きの直線を軸として回転する */
   private fun <T : Transformable<T>>
         T.rotate(axis: (T) -> Vector3d, angle: Angle): T
   {
      return rotate(
         Line3d(referencePoint, axis(this)),
         angle
      )
   }

   val alphanumericPlate: AlphanumericPlate get() {
      return AlphanumericPlate(frontVector, bottomVector, referencePoint)
         .rotate(AlphanumericPlate::frontVector, 15.deg)
         .translate(backVector, 69.mm)
         .translate(topVector, 85.mm)
   }

   val thumbPlate: ThumbPlate get() {
      return ThumbPlate(frontVector, bottomVector, referencePoint)
         .rotate(ThumbPlate::backVector, 69.deg)
         .rotate(ThumbPlate::leftVector, 7.deg)
         .rotate(ThumbPlate::bottomVector, 8.deg)
         .translate(rightVector, 40.mm)
         .translate(backVector, 18.mm)
         .translate(topVector, 49.mm)
   }

   val frontRotaryEncoderKnob get() = FrontRotaryEncoderKnob(alphanumericPlate)
   val backRotaryEncoderKnob get() = BackRotaryEncoderKnob(alphanumericPlate)
   val backRotaryEncoderGear get() = BackRotaryEncoderGear(alphanumericPlate, velocityRatio = 1.0)
   val leftOuterRotaryEncoderKnob get() = LeftOuterRotaryEncoderKnob(this)
   val leftInnerRotaryEncoderKnob get() = LeftInnerRotaryEncoderKnob(leftOuterRotaryEncoderKnob)

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = Case(frontVector, bottomVector, referencePoint)
}

fun ScadParentObject.case(case: Case): ScadObject {
   return (
      union {
         alphanumericFrontCase(case)
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

      - hullAlphanumericPlate(case.alphanumericPlate, HullAlphanumericConfig())
      - alphanumericBottomCave(case)

      + difference {
         alphanumericPlate(case.alphanumericPlate)
         backRotaryEncoderInsertionHole(case)
      }

      + thumbPlate(case.thumbPlate)

      + backRotaryEncoderMountPlate(case)

      - frontRotaryEncoderHole(case.alphanumericPlate, case.frontRotaryEncoderKnob)
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

fun alphanumericFrontPlane(thumbPlate: ThumbPlate, offset: Size): Plane3d
      = thumbPlate.frontPlane.translate(Vector3d.Y_UNIT_VECTOR, -offset)

private fun ScadParentObject.alphanumericFrontCase(case: Case): ScadObject {
   val topPlane = alphanumericTopPlane(case.alphanumericPlate, 3.mm)
   val bottomPlane = alphanumericBottomPlane(case, 0.mm)
   val leftPlane = alphanumericLeftPlane(case.alphanumericPlate, 1.5.mm)
   val rightPlane = alphanumericRightPlane(case.alphanumericPlate, 1.5.mm)
   val backSlopePlane = alphanumericBackSlopePlane(case.alphanumericPlate, 1.5.mm)
   val backPlane = alphanumericBackPlane(case, 1.5.mm)
   val frontPlane = alphanumericFrontPlane(case.thumbPlate, 1.5.mm)

   return intersection {
      distortedCube(topPlane, leftPlane, backPlane, rightPlane, frontPlane, bottomPlane)
      distortedCube(backSlopePlane, leftPlane, backPlane, rightPlane, frontPlane, bottomPlane)
   }
}

private fun ScadParentObject.alphanumericTopCave(alphanumericPlate: AlphanumericPlate): ScadObject {
   return union {
      hullAlphanumericPlate(
         alphanumericPlate,
         HullAlphanumericConfig(
            layerOffset = -KeySwitch.TRAVEL,
            frontBackOffset = 20.mm,
            leftRightOffset = 20.mm
         )
      )

      hullAlphanumericPlate(alphanumericPlate, HullAlphanumericConfig())
   }
}

private fun ScadParentObject.alphanumericBottomCave(case: Case): ScadObject {
   return intersection {
      hullAlphanumericPlate(
         case.alphanumericPlate,
         HullAlphanumericConfig(layerOffset = 100.mm)
      )

      distortedCube(
         alphanumericTopPlane(case.alphanumericPlate, 0.mm),
         alphanumericLeftPlane(case.alphanumericPlate, 0.mm),
         alphanumericBackPlane(case, 0.mm),
         alphanumericRightPlane(case.alphanumericPlate, 0.mm),
         alphanumericFrontPlane(case.thumbPlate, 0.mm),
         alphanumericBottomPlane(case, 1.mm)
      )
   }
}

// =============================================================================

private fun ScadParentObject.thumbCase(plate: ThumbPlate): ScadObject {
   return hullThumbPlate(plate, layerOffset = 12.mm, leftRightOffset = 7.mm, frontOffset = 1.5.mm)
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
   offset: Size
): ScadObject {
   val caseLeftPlane  = backRotaryEncoderCaseLeftPlane (case.alphanumericPlate, offset)
   val caseRightPlane = backRotaryEncoderCaseRightPlane(case.alphanumericPlate, offset)

   val caseBackPlane = backRotaryEncoderCaseBackPlane(case, offset)
   val caseBackSlopePlane = backRotaryEncoderCaseSlopePlane(case.alphanumericPlate, case.backRotaryEncoderGear.gear, offset)
   val caseTopPlane = backRotaryEncoderCaseTopPlane(case, offset)
   val caseFrontPlane = backRotaryEncoderCaseFrontPlane(case, offset)
   val caseBottomPlane = backRotaryEncoderCaseBottomPlane(case, 0.mm)

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
      backRotaryEncoderCase(case, offset = 1.5.mm)

      hullAlphanumericPlate(
         case.alphanumericPlate,
         HullAlphanumericConfig(layerOffset = 10.mm)
      )
   }
}

fun ScadParentObject.backRotaryEncoderCave(case: Case): ScadObject {
   return backRotaryEncoderCase(case, offset = 0.mm)
}

fun ScadParentObject.backRotaryEncoderInsertionHole(case: Case): ScadObject {
   val rotaryEncoder = case.backRotaryEncoderGear.rotaryEncoder

   return (
      intersection {
         backRotaryEncoderCase(case, offset = 0.mm)

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
         backRotaryEncoderCase(case, offset = 0.mm)

         cube(
            Cube(
               rotaryEncoder.referencePoint,
               Size3d(100.mm, 12.mm, 40.mm),
               backRotaryEncoderCaseBackPlane(case, 0.mm).normalVector,
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
         backRotaryEncoderCase(case, offset = 1.5.mm)

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

   return locale(
      knob.referencePoint
         .translate(knob.bottomVector, BackRotaryEncoderKnob.GEAR_THICKNESS)
   ) {
      rotate(
         -Vector3d.Z_UNIT_VECTOR angleWith knob.bottomVector,
         -Vector3d.Z_UNIT_VECTOR vectorProduct knob.bottomVector,
      ) {
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

private fun ScadParentObject.frontRotaryEncoderHole(
   alphanumericPlate: AlphanumericPlate,
   knob: FrontRotaryEncoderKnob
): ScadObject {
   val rotaryEncoder = knob.rotaryEncoder

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
            translate(z = (-1.5).mm) {
               cylinder(FrontRotaryEncoderKnob.HEIGHT * 2,
                  FrontRotaryEncoderKnob.RADIUS + 0.7.mm, `$fa`)
            }
         }
      }

      intersection {
         locale(knob.referencePoint) {
            rotate(
               Vector3d.Z_UNIT_VECTOR angleWith     rotaryEncoder.topVector,
               Vector3d.Z_UNIT_VECTOR vectorProduct rotaryEncoder.topVector
            ) {
               translate((-50).mm, (-50).mm, (-1.5).mm) {
                  cube(100.mm, 100.mm, 100.mm)
               }
            }
         }

         hullColumn(
            alphanumericPlate.columns[FrontRotaryEncoderKnob.COLUMN_INDEX],
            alphanumericPlate.columns.getOrNull(FrontRotaryEncoderKnob.COLUMN_INDEX - 1),
            alphanumericPlate.columns.getOrNull(FrontRotaryEncoderKnob.COLUMN_INDEX + 1),
            HullAlphanumericConfig(
               layerOffset = 20.mm,
               frontBackOffset = 20.mm,
               columnOffset = 1.mm
            )
         )
      }
   }
}

private fun ScadParentObject.frontRotaryEncoderMountPlate
      (knob: FrontRotaryEncoderKnob): ScadObject
{
   val rotaryEncoder = knob.rotaryEncoder

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
