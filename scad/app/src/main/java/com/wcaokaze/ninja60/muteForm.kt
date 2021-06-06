package com.wcaokaze.ninja60

import com.wcaokaze.scadwriter.*

fun ScadWriter.muteForm() {
   difference {
      cube(KeyPlate.SIZE.x, KeyPlate.SIZE.y, topPlateHeight, center = true)
      cube(topPlateHoleSize.x, topPlateHoleSize.y, topPlateHeight, center = true)
   }
}
