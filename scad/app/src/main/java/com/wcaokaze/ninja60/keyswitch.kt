package com.wcaokaze.ninja60

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

val topPlateThickness = 1.5.mm
val topPlateHeight = 5.mm - topPlateThickness

val topPlateHoleSize = Size2d(14.mm, 14.mm)

fun ScadWriter.muteForm() {
   difference {
      cube(19.mm, 19.mm, topPlateHeight, center = true)
      cube(topPlateHoleSize.x, topPlateHoleSize.y, topPlateHeight, center = true)
   }
}
