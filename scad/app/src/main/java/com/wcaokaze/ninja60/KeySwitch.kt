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

fun ScadParentObject.switchHole(): ScadObject {
   val plateHoleSize = 14.5.mm
   val plateThickness = 1.5.mm
   val caveSize = 16.mm

   return union {
      translate(-caveSize / 2, -caveSize / 2, -(KeySwitch.BOTTOM_HEIGHT + 0.5.mm)) {
         cube(caveSize, caveSize, KeySwitch.BOTTOM_HEIGHT + 0.5.mm - plateThickness)
      }

      translate(-plateHoleSize / 2, -plateHoleSize / 2, -(1.5.mm + 0.5.mm)) {
         cube(plateHoleSize, plateHoleSize, 2.0.mm)
      }
   }
}

/**
 * MXキースイッチの左右の凹みにハマる突起。
 *
 * まずプレートから[switchHole]を[difference]して穴を開けたあとコイツをつけるといい
 */
fun ScadParentObject.switchSideHolder(): ScadObject {
   val ySize = 3.6.mm
   val zSize = 3.7.mm
   val cylinderRadius = 1.mm

   fun ScadParentObject.holder(): ScadObject {
      return union {
         translate(7.25.mm, -ySize / 2, -zSize) {
            cube(1.5.mm, ySize, zSize)
         }

         hull {
            translate(7.25.mm,  ySize / 2, (-1.5).mm) { cube(0.01.mm, 0.01.mm, 0.01.mm, center = true) }
            translate(7.25.mm, -ySize / 2, (-1.5).mm) { cube(0.01.mm, 0.01.mm, 0.01.mm, center = true) }

            translate(x = 7.25.mm, z = -(zSize - cylinderRadius)) {
               rotate(x = 90.deg) {
                  cylinder(height = ySize, cylinderRadius, center = true)
               }
            }
         }
      }
   }

   return union {
      holder()
      mirror(x = 1.mm, y = 0.mm, z = 0.mm) { holder() }
   }
}
