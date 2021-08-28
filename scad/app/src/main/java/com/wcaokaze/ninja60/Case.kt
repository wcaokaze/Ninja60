package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

object Case

fun ScadWriter.case(case: Case) {
   val alphanumericPlate = AlphanumericPlate()
      .rotate(Line3d.Y_AXIS, (-15).deg)
      .translate(x = 0.mm, y = 69.mm, z = 85.mm)

   val thumbPlate = ThumbPlate()
      .rotate(Line3d.Y_AXIS, 69.deg)
      .rotate(Line3d.X_AXIS, (-7).deg)
      .rotate(Line3d.Z_AXIS, (-8).deg)
      .translate(x = 66.mm, y = 0.mm, z = 53.mm)

   difference {
      union {
         alphanumericFrontCase()
         thumbCase(thumbPlate)
      }

      union {
         alphanumericCave(alphanumericPlate)
         thumbCave(thumbPlate)
      }
   }

   translate((-62).mm, (-108).mm, 0.mm) {
      cube(102.mm, 70.mm, 80.mm)
   }

   alphanumericPlate(alphanumericPlate)
   thumbPlate(thumbPlate)
}

private fun ScadWriter.alphanumericFrontCase() {
   translate((-47).mm, (14).mm, 0.mm) {
      rotate(y = (-15).deg, z = 7.deg) {
         cube(122.mm, 30.mm, 60.mm)
      }
   }
}

private fun ScadWriter.alphanumericCave(plate: AlphanumericPlate) {
   union {
      hullAlphanumericPlate(plate, frontBackOffset = 20.mm)
      hullAlphanumericPlate(plate, layerOffset = 40.mm)
   }
}

private fun ScadWriter.thumbCase(plate: ThumbPlate) {
   /*
   translate(66.mm, 0.mm, 0.mm) {
      rotate(z = (-8).deg) {
         rotate(x = (-7).deg) {
            rotate(y = 69.deg) {
               translate(y = 14.mm, z = 32.mm) {
                  cube(68.mm, 54.mm, 42.mm, center = true)
               }
            }
         }
      }
   }
   */

   hullThumbPlate(plate, layerOffset = 12.mm, leftRightOffset = 5.mm, frontOffset = 2.mm)
}

private fun ScadWriter.thumbCave(plate: ThumbPlate) {
   union {
      hullThumbPlate(plate, leftRightOffset = 20.mm, frontOffset = 20.mm)
      hullThumbPlate(plate, layerOffset = 10.mm)
   }
}
