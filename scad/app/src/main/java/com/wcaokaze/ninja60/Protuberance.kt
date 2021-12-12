package com.wcaokaze.ninja60

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

fun ScadParentObject.linearProtuberance(
   length: Size,
   protuberanceRadius: Size
): ScadObject {
   return minkowski {
      cube(length, 0.01.mm, 0.01.mm)
      sphere(protuberanceRadius, `$fa`)
   }
}

fun ScadParentObject.circularProtuberance(
   circleRadius: Size,
   protuberanceRadius: Size
): ScadObject {
   return minkowski {
      difference {
         cylinder(0.01.mm, circleRadius, `$fa`)
         cylinder(0.01.mm, circleRadius - 0.01.mm, `$fa`)
      }

      sphere(protuberanceRadius, `$fa`)
   }
}
