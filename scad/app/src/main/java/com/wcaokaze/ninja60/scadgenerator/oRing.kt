package com.wcaokaze.ninja60.scadgenerator

import com.wcaokaze.ninja60.scadgenerator.scadwriter.*
import com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation.*

fun ScadWriter.prepareORingModule() {
   writeRawScad("""
      module o_ring(tightening = 0) {
         difference() {
            polygon_pyramid(16, 4.3, h = 2);

            minkowski() {
               cube([1, 0.01, 0.01], center = true);
               polygon_pyramid(8, 2.97 - tightening * 2, h = 2);
            }
         }
      }
   """)
}

fun ScadWriter.oRing(tightening: Size = 0.mm) {
   writeRawScad("o_ring($tightening);")
}
