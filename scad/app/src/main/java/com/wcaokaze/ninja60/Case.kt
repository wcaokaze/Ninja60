package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

object Case

fun ScadWriter.case(case: Case) {
   val alphanumericPlate = AlphanumericPlate()
      .rotate(Line3d.Y_AXIS, (-15).deg)
      .translate(x = 0.mm, y = 69.mm, z = 32.mm)

   difference {
      translate((-47).mm, (14).mm, (-53).mm) {
         rotate(y = (-15).deg, z = 7.deg) {
            cube(122.mm, 30.mm, 60.mm)
         }
      }

      hullAlphanumericPlate(alphanumericPlate, frontBackOffset = 20.mm)
      hullAlphanumericPlate(alphanumericPlate, layerOffset = 40.mm)
   }

   alphanumericPlate(alphanumericPlate)
}
