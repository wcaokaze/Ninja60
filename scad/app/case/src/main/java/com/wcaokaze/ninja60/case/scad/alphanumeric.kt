package com.wcaokaze.ninja60.case.scad

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.case.*
import com.wcaokaze.ninja60.parts.key.*
import com.wcaokaze.ninja60.parts.key.alphanumeric.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.ninja60.shared.scadutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

fun ScadParentObject.alphanumericCase(
   case: Case,
   bottomOffset: Size = 0.mm,
   otherOffsets: Size = 0.mm
): ScadObject {
   val xLines = listOf(
      case.rightVectorLine.translate(y = (-300).mm, z = (-300).mm),
      case.rightVectorLine.translate(y = (-300).mm, z =   300 .mm),
      case.rightVectorLine.translate(y =   300 .mm, z = (-300).mm),
      case.rightVectorLine.translate(y =   300 .mm, z =   300 .mm),
   )

   val leftPlane  = Plane3d.YZ_PLANE.translate((-300).mm)
   val rightPlane = Plane3d.YZ_PLANE.translate(  300 .mm)

   val knobColumn = case.alphanumericPlate.columns[Case.FRONT_ROTARY_ENCODER_COLUMN_INDEX]
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
                  alphanumericFrontPlaneRight(case.alphanumericPlate, case.thumbHomeKey, otherOffsets),
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
      alphanumericPlate.columns.subList(0, Case.FRONT_ROTARY_ENCODER_COLUMN_INDEX),
      offset
   )
}

fun alphanumericTopPlaneRight(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d {
   return alphanumericTopPlane(
      alphanumericPlate,
      alphanumericPlate.columns.subList(
         Case.FRONT_ROTARY_ENCODER_COLUMN_INDEX, alphanumericPlate.columns.size),
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
      alphanumericPlate.columns.subList(0, Case.FRONT_ROTARY_ENCODER_COLUMN_INDEX),
      surfaceVector = Vector3d.Z_UNIT_VECTOR,
      offset + Case.ALPHANUMERIC_FRONT_LEFT_MARGIN
   )
}

fun alphanumericFrontPlaneRight(
   alphanumericPlate: AlphanumericPlate,
   thumbHomeKey: KeySwitch,
   offset: Size
): Plane3d {
   return alphanumericFrontPlane(
      alphanumericPlate,
      alphanumericPlate.columns.subList(
         Case.FRONT_ROTARY_ENCODER_COLUMN_INDEX, alphanumericPlate.columns.size),
      surfaceVector = thumbHomeKey.leftVector,
      offset + Case.ALPHANUMERIC_FRONT_RIGHT_MARGIN
   )
}

/**
 * @param surfaceVector
 * 表面のベクトル。返り値の平面はこのベクトルと平行
 * (すなわち法線ベクトルがこのベクトルと垂直)であることが保証される。
 * 一応ケースに対して上向きであることを期待してます
 */
private fun alphanumericFrontPlane(
   alphanumericPlate: AlphanumericPlate,
   columns: List<AlphanumericColumn>,
   surfaceVector: Vector3d,
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
         Plane3d(it.pointA, it.vectorAB vectorProduct surfaceVector)
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
