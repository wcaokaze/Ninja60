package com.wcaokaze.ninja60

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

fun ScadWriter.hullPoints(vararg points: Point3d) {
   hull {
      for (point in points) {
         translate(point - Point3d.ORIGIN) {
            cube(0.01.mm, 0.01.mm, 0.01.mm, center = true)
         }
      }
   }
}

fun ScadWriter.hullPoints(points: List<Point3d>) {
   hullPoints(*points.toTypedArray())
}
