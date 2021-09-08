package com.wcaokaze.ninja60

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

fun ScadParentObject.hullPoints(vararg points: Point3d): Hull {
   return hull {
      for (point in points) {
         translate(point - Point3d.ORIGIN) {
            cube(0.01.mm, 0.01.mm, 0.01.mm, center = true)
         }
      }
   }
}

fun ScadParentObject.hullPoints(points: List<Point3d>): Hull
      = hullPoints(*points.toTypedArray())
