package com.wcaokaze.ninja60.shared.calcutil

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * 法線ベクトル方向を正としてこの平面と[point]の位置関係を比較します
 */
operator fun Plane3d.compareTo(point: Point3d): Int {
   val perpendicular = perpendicular(point)
   return when {
      perpendicular.norm < 0.001.mm -> 0
      perpendicular angleWith normalVector in (-90).deg..90.deg -> 1
      else -> -1
   }
}

/**
 * 法線ベクトルの向きを正として2つの平面の位置を比較します
 * 2つの平面の法線ベクトルは同じ向きである必要があります
 */
operator fun Plane3d.compareTo(another: Plane3d): Int {
   require(normalVector angleWith another.normalVector < 0.01.deg)

   val line = Line3d(Point3d.ORIGIN, normalVector)
   val vAB = Vector3d(this intersection line, another intersection line)

   return when {
      vAB.norm < 0.001.mm -> 0
      vAB angleWith normalVector in (-90).deg..90.deg -> -1
      else -> 1
   }
}

/**
 * 法線ベクトルの向きを正としたとき、より大きい位置にある平面を返します
 * 2つの平面の法線ベクトルは同じ向きである必要があります
 */
fun max(a: Plane3d, b: Plane3d): Plane3d {
   return if (a < b) {
      b
   } else {
      a
   }
}
