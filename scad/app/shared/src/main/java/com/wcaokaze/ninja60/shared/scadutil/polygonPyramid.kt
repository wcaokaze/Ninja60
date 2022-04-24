package com.wcaokaze.ninja60.shared.scadutil

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

fun ScadParentObject.polygonPyramid(n: Int, height: Size, radius: Size): ScadObject {
   return linearExtrude(height) {
      polygon(
         ((180.deg / n)..(360.deg / n) step (360.deg + 180.deg / n)).map {
            Point2d(
               Point(radius * sin(it)),
               Point(radius * cos(it))
            )
         }
      )
   }
}
