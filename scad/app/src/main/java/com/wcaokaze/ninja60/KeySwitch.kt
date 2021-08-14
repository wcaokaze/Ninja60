package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

data class KeySwitch(
   val center: Point3d,

   /** このスイッチの下向きの方向を表すベクトル。 */
   val bottomVector: Vector3d,
   /** このスイッチの手前方向を表すベクトル。 */
   val frontVector: Vector3d
) {
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
}

fun KeySwitch.translate(distance: Size3d)
      = KeySwitch(center.translate(distance), bottomVector, frontVector)

fun KeySwitch.translate(distance: Vector3d)
      = KeySwitch(center.translate(distance), bottomVector, frontVector)

fun KeySwitch.translate(direction: Vector3d, distance: Size)
      = KeySwitch(center.translate(direction, distance), bottomVector, frontVector)

fun KeySwitch.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm
): KeySwitch = translate(Size3d(x, y, z))

fun KeySwitch.rotate(axis: Line3d, angle: Angle) = KeySwitch(
   center.rotate(axis, angle),
   bottomVector.rotate(axis.vector, angle),
   frontVector.rotate(axis.vector, angle)
)

private fun KeySwitch.keyPlate(size: Size2d)
      = KeyPlate(center, size, -bottomVector, frontVector)

fun ScadWriter.switchHole(keySwitch: KeySwitch) {
   /* ボトムハウジングの突起部分にハメる穴。本来1.5mmのプレートを使うところ */
   val mountPlateHole = keySwitch.keyPlate(Size2d(14.5.mm, 14.5.mm))

   /* スイッチが入る穴。 */
   val switchHole = keySwitch.keyPlate(Size2d(16.mm, 16.mm))

   union {
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
fun ScadWriter.switchSideHolder(keySwitch: KeySwitch) {
   fun Point3d.dx(dx: Size) = translate(keySwitch.frontVector vectorProduct keySwitch.bottomVector, dx)
   fun Point3d.dy(dy: Size) = translate(keySwitch.frontVector, dy)
   fun Point3d.dz(dz: Size) = translate(keySwitch.bottomVector, dz)

   val ySize = 3.6.mm
   val zSize = 3.7.mm
   val cylinderRadius = 1.mm

   fun ScadWriter.pillar(dx: Size) {
      val pillarCenter = keySwitch.center.dx(dx)

      val points = ArrayList<Point3d>()

      for (x in listOf(0.75.mm, (-0.75).mm)) {
         for (y in listOf(ySize / 2, -ySize / 2)) {
            for (z in listOf(0.mm, zSize)) {
               points += pillarCenter.dx(x).dy(y).dz(z)
            }
         }
      }

      hullPoints(points)
   }

   fun ScadWriter.point(dx: Size, dy: Size, dz: Size) {
      translate(keySwitch.center.dx(dx).dy(dy).dz(dz) - Point3d.ORIGIN) {
         cube(0.01.mm, 0.01.mm, 0.01.mm, center = true)
      }
   }

   union {
      pillar(  8 .mm)
      pillar((-8).mm)

      for (dx in listOf((-7.25).mm, 7.25.mm)) {
         hull {
            point(dx,  ySize / 2, 1.5.mm)
            point(dx, -ySize / 2, 1.5.mm)

            translate(keySwitch.center.dx(dx).dz(zSize - cylinderRadius) - Point3d.ORIGIN) {
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
