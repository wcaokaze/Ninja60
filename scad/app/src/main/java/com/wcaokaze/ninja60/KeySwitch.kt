package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

data class KeySwitch(
   override val referencePoint: Point3d,
   val layoutSize: LayoutSize,

   override val bottomVector: Vector3d,
   override val frontVector: Vector3d,
) : Transformable<KeySwitch> {
   companion object {
      val TRAVEL = 4.mm

      /** 押し込んでいない状態でのステムの先からスイッチの上面までの距離 */
      val STEM_HEIGHT = 4.mm

      /** (ステムを除いた)キースイッチの高さ */
      val HEIGHT = 11.1.mm

      /** トッププレートの表面からスイッチの底面(足の先ではない)までの距離 */
      val BOTTOM_HEIGHT = 5.mm

      /** スイッチの上面(ステムの先ではない)からトッププレートまでの距離 */
      val TOP_HEIGHT = HEIGHT - BOTTOM_HEIGHT
   }

   /** [大きさ][layoutSize]が1Uの[KeySwitch] */
   constructor(center: Point3d, bottomVector: Vector3d, frontVector: Vector3d)
         : this(center, LayoutSize(1.0, 1.0), bottomVector, frontVector)

   /**
    * キーの大きさ。厳密に言うとキースイッチの大きさは変わらないので
    * 配置される際の配列としてのキーの大きさ。
    * アルファベット1キーのサイズを1.0とした割合で表す、いわゆる「U」を単位とするもの。
    */
   data class LayoutSize(val x: Double, val y: Double)

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = KeySwitch(referencePoint, layoutSize, bottomVector, frontVector)
}

operator fun Size2d.times(layoutSize: KeySwitch.LayoutSize) = Size2d(
   x * layoutSize.x,
   y * layoutSize.y
)

/**
 * このKeySwitchの位置に配置された[KeyPlate]を返す
 * @param size 1Uでのプレートのサイズ
 */
fun KeySwitch.plate(size: Size2d) = KeyPlate(
   referencePoint,
   size * layoutSize,
   bottomVector,
   frontVector
)

fun ScadParentObject.switchHole(keySwitch: KeySwitch): ScadObject {
   fun KeySwitch.keyPlate(size: Size2d)
         = KeyPlate(referencePoint, size, bottomVector, frontVector)

   /* ボトムハウジングの突起部分にハメる穴。本来1.5mmのプレートを使うところ */
   val mountPlateHole = keySwitch.keyPlate(Size2d(14.5.mm, 14.5.mm))

   /* スイッチが入る穴。 */
   val switchHole = keySwitch.keyPlate(Size2d(16.mm, 16.mm))

   return union {
      hullPoints(
         mountPlateHole.translate(keySwitch.bottomVector, -(0.5).mm).points +
         mountPlateHole.translate(keySwitch.bottomVector,   2.0 .mm).points
      )

      hullPoints(
         switchHole.translate(keySwitch.bottomVector,                           1.5.mm).points +
         switchHole.translate(keySwitch.bottomVector, KeySwitch.BOTTOM_HEIGHT + 0.5.mm).points
      )
   }
}

/**
 * MXキースイッチの左右の凹みにハマる突起。
 *
 * まずプレートから[switchHole]を[difference]して穴を開けたあとコイツをつけるといい
 */
fun ScadParentObject.switchSideHolder(keySwitch: KeySwitch): ScadObject {
   fun Point3d.dx(dx: Size) = translate(keySwitch.rightVector, dx)
   fun Point3d.dy(dy: Size) = translate(keySwitch.frontVector, dy)
   fun Point3d.dz(dz: Size) = translate(keySwitch.bottomVector, dz)

   val ySize = 3.6.mm
   val zSize = 3.7.mm
   val cylinderRadius = 1.mm

   fun ScadParentObject.pillar(dx: Size): ScadObject {
      val pillarCenter = keySwitch.referencePoint.dx(dx)

      val points = ArrayList<Point3d>()

      for (x in listOf(0.75.mm, (-0.75).mm)) {
         for (y in listOf(ySize / 2, -ySize / 2)) {
            for (z in listOf(0.mm, zSize)) {
               points += pillarCenter.dx(x).dy(y).dz(z)
            }
         }
      }

      return hullPoints(points)
   }

   fun ScadParentObject.point(dx: Size, dy: Size, dz: Size): ScadObject {
      return translate(keySwitch.referencePoint.dx(dx).dy(dy).dz(dz) - Point3d.ORIGIN) {
         cube(0.01.mm, 0.01.mm, 0.01.mm, center = true)
      }
   }

   return union {
      pillar(  8 .mm)
      pillar((-8).mm)

      for (dx in listOf((-7.25).mm, 7.25.mm)) {
         hull {
            point(dx,  ySize / 2, 1.5.mm)
            point(dx, -ySize / 2, 1.5.mm)

            translate(keySwitch.referencePoint.dx(dx).dz(zSize - cylinderRadius) - Point3d.ORIGIN) {
               rotate(
                  Vector3d.Z_UNIT_VECTOR angleWith keySwitch.frontVector,
                  Vector3d.Z_UNIT_VECTOR vectorProduct keySwitch.frontVector
               ) {
                  cylinder(height = ySize, cylinderRadius, center = true, fa = `$fa`)
               }
            }
         }
      }
   }
}
