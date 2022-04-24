package com.wcaokaze.ninja60.shared.calcutil

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.foundation.*

/** 指定したベクトルの向きを正として2点を比較するComparator。 */
class PointOnVectorComparator(val vector: Vector3d) : Comparator<Point3d> {
   override fun compare(o1: Point3d, o2: Point3d): Int
         = Plane3d(o1, vector).compareTo(Plane3d(o2, vector))
}

/** 指定したベクトルの向きを正としたとき、より大きい位置にある点を返します */
fun max(vector: Vector3d, a: Point3d, b: Point3d): Point3d {
   return if (Plane3d(a, vector) < Plane3d(b, vector)) {
      b
   } else {
      a
   }
}
