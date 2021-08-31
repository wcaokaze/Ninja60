package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

object Case

fun ScadWriter.case(case: Case) {
   val alphanumericPlate = AlphanumericPlate()
      .rotate(Line3d.Y_AXIS, (-15).deg)
      .translate(x = 0.mm, y = 69.mm, z = 85.mm)

   val thumbPlate = ThumbPlate()
      .rotate(Line3d.Y_AXIS, 69.deg)
      .rotate(Line3d.X_AXIS, (-7).deg)
      .rotate(Line3d.Z_AXIS, (-8).deg)
      .translate(x = 66.mm, y = 0.mm, z = 53.mm)

   difference {
      union {
         alphanumericFrontCase(alphanumericPlate)
         thumbCase(thumbPlate)
      }

      union {
         alphanumericCave(alphanumericPlate)
         thumbCave(thumbPlate)
      }
   }

   translate((-62).mm, (-108).mm, 0.mm) {
      cube(102.mm, 70.mm, 80.mm)
   }

   alphanumericPlate(alphanumericPlate)
   thumbPlate(thumbPlate)
}

private fun ScadWriter.distortedCube(
   topPlane: Plane3d,
   leftPlane: Plane3d,
   backPlane: Plane3d,
   rightPlane: Plane3d,
   frontPlane: Plane3d,
   bottomPlane: Plane3d
) {
   hullPoints(
      listOf(leftPlane, rightPlane).flatMap { x ->
         listOf(frontPlane, backPlane).flatMap { y ->
            listOf(bottomPlane, topPlane).map { z ->
               x intersection y intersection z
            }
         }
      }
   )
}

private fun alphanumericTopPlane(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d {
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

private fun alphanumericBottomPlane(offset: Size): Plane3d
      = Plane3d.XY_PLANE.translate(Vector3d.Z_UNIT_VECTOR, -offset)

private fun alphanumericLeftPlane(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d
      = alphanumericPlate.leftmostPlane.let { it.translate(it.normalVector, -offset) }
private fun alphanumericRightPlane(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d
      = alphanumericPlate.rightmostPlane.let { it.translate(it.normalVector, offset) }

private fun alphanumericBackPlane(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d {
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

private fun alphanumericFrontPlane(offset: Size): Plane3d {
   return Plane3d.ZX_PLANE
      .translate(Vector3d.Y_UNIT_VECTOR, 9.mm)
      .translate(Vector3d.Y_UNIT_VECTOR, -offset)
}

private fun ScadWriter.alphanumericFrontCase(alphanumericPlate: AlphanumericPlate) {
   val leftRightOffset = 1.5.mm

   val topPlane = alphanumericTopPlane(alphanumericPlate, 3.mm)
   val bottomPlane = alphanumericBottomPlane(0.mm)
   val leftPlane = alphanumericLeftPlane(alphanumericPlate, leftRightOffset)
   val rightPlane = alphanumericRightPlane(alphanumericPlate, leftRightOffset)
   val backPlane = alphanumericBackPlane(alphanumericPlate, 1.5.mm)
   val frontPlane = alphanumericFrontPlane(1.5.mm)

   /** 手前と奥の境目の平面 */
   val middlePlane = Plane3d(
      alphanumericPlate.columns.map { it.referencePoint } .average(),
      Vector3d.Y_UNIT_VECTOR
   )

   fun ScadWriter.frontCase() {
      distortedCube(topPlane, leftPlane, middlePlane.translate(Vector3d.Y_UNIT_VECTOR),
         rightPlane, frontPlane, bottomPlane)
   }

   fun ScadWriter.backCase() {
      intersection {
         hullAlphanumericPlate(alphanumericPlate, layerOffset = 100.mm,
            frontBackOffset = 1.5.mm, leftRightOffset, columnOffset = 1.mm)

         distortedCube(topPlane, leftPlane, backPlane, rightPlane,
            middlePlane.translate(-Vector3d.Y_UNIT_VECTOR), bottomPlane)
      }
   }

   union {
      frontCase()
      backCase()
   }
}

private fun ScadWriter.alphanumericCave(alphanumericPlate: AlphanumericPlate) {
   union {
      hullAlphanumericPlate(alphanumericPlate, frontBackOffset = 20.mm, leftRightOffset = 20.mm)

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

private fun ScadWriter.thumbCase(plate: ThumbPlate) {
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

   hullThumbPlate(plate, layerOffset = 12.mm, leftRightOffset = 5.mm, frontOffset = 2.mm)
}

private fun ScadWriter.thumbCave(plate: ThumbPlate) {
   union {
      hullThumbPlate(plate, leftRightOffset = 20.mm, frontOffset = 20.mm)
      hullThumbPlate(plate, layerOffset = 10.mm)
   }
}
