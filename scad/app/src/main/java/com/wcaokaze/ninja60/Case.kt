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
}

fun ScadParentObject.case(case: Case): ScadObject {
   return (
      alphanumericFrontCase(case.alphanumericPlate) + thumbCase(case.thumbPlate)
      - (alphanumericCave(case.alphanumericPlate) + thumbCave(case.thumbPlate))
      + alphanumericPlate(case.alphanumericPlate)
      + thumbPlate(case.thumbPlate)

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

private fun ScadParentObject.alphanumericCave(alphanumericPlate: AlphanumericPlate): ScadObject {
   return union {
      hullAlphanumericPlate(alphanumericPlate, layerOffset = -KeySwitch.TRAVEL, frontBackOffset = 20.mm, leftRightOffset = 20.mm)
      hullAlphanumericPlate(alphanumericPlate)

      intersection {
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
}

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
