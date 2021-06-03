package com.wcaokaze.ninja60

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

val topPlateThickness = 1.5.mm
val topPlateHeight = 5.mm - topPlateThickness

val topPlateHoleSize = Size2d(14.mm, 14.mm)

fun ScadWriter.topPlate() {
   difference {
      cube(19.05.mm * 2, 19.05.mm * 2, 1.5.mm)
      translate(19.05.mm * 0.5, 19.05.mm * 0.5) { cube(14.mm, 14.mm, 6.mm, center = true) }
      translate(19.05.mm * 0.5, 19.05.mm * 1.5) { cube(14.mm, 14.mm, 6.mm, center = true) }
      translate(19.05.mm * 1.5, 19.05.mm * 0.5) { cube(14.mm, 14.mm, 6.mm, center = true) }
      translate(19.05.mm * 1.5, 19.05.mm * 1.5) { cube(14.mm, 14.mm, 6.mm, center = true) }
   }
}
