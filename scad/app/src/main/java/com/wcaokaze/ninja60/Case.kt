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

private fun ScadWriter.alphanumericFrontCase(alphanumericPlate: AlphanumericPlate) {
   val topPlane = run {
      // 上面の平面を算出する。
      // 各Columnの一番手前の点から2点を選び、
      // その2点を通る平面が他のすべての点より上にあるとき使える

      val frontVector = alphanumericPlate.columns.map { it.frontVector } .sum()

      val points = alphanumericPlate.columns
         .flatMap { column ->
            val plate = column.keySwitches.last().plate(AlphanumericPlate.KEY_PLATE_SIZE)
            listOf(plate.frontLeft, plate.frontRight)
         }

      points.flatMap { left ->
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
   }

   val bottomPlane = Plane3d.XY_PLANE
   val leftPlane = alphanumericPlate.leftmostPlane
   val rightPlane = alphanumericPlate.rightmostPlane
   val backPlane = Plane3d.ZX_PLANE.translate(Vector3d.Y_UNIT_VECTOR, 58.mm)
   val frontPlane = Plane3d.ZX_PLANE.translate(Vector3d.Y_UNIT_VECTOR, 18.mm)

   val xPlanes = listOf(
      leftPlane .translate(-leftPlane.normalVector, 3.mm),
      rightPlane.translate(rightPlane.normalVector, 3.mm)
   )

   val yPlanes = listOf(
      frontPlane,
      backPlane
   )

   val zPlanes = listOf(
      bottomPlane,
      topPlane.translate(topPlane.normalVector, 3.mm)
   )

   val points = xPlanes.flatMap { x ->
      yPlanes.flatMap { y ->
         zPlanes.map { z ->
            x intersection y intersection z
         }
      }
   }

   hullPoints(points)
}

private fun ScadWriter.alphanumericCave(plate: AlphanumericPlate) {
   union {
      hullAlphanumericPlate(plate, frontBackOffset = 20.mm, leftRightOffset = 20.mm)
      hullAlphanumericPlate(plate, layerOffset = 60.mm)
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
