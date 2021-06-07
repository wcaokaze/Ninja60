package com.wcaokaze.ninja60

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

val topPlateThickness = 1.5.mm
val topPlateHeight = 5.mm - topPlateThickness

val topPlateHoleSize = Size2d(14.mm, 14.mm)

data class TopPlate(
   val alphanumericColumns: AlphanumericColumns
) {
   companion object {
      operator fun invoke() = TopPlate(
         AlphanumericColumns(-Keycap.THICKNESS - KeySwitch.STEM_HEIGHT - KeySwitch.TOP_HEIGHT)
      )
   }
}

fun ScadWriter.topPlate() {
   val topPlate = TopPlate()

   for (c in topPlate.alphanumericColumns.columns) {
      for (p in c.keyPlates) {
         hullPoints(p.points)
      }
   }
}
