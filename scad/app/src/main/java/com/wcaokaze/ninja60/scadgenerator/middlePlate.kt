package com.wcaokaze.ninja60.scadgenerator

import com.wcaokaze.ninja60.scadgenerator.scadwriter.*
import com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation.*

val middlePlateFa = 8.0

fun ScadWriter.prepareMiddlePlateModule() {
   val fa = "\$fa"
   writeRawScad("""
      module key_switch_hole() {
         translate([0, 0, 1]) union() {
            translate([-4.25 / 2, -16 / 2, 2.47]) cube([4.25, 16, 2.53]);

            hull() {
               translate([-14.75 / 2, -14.75 / 2, 3.2]) cube([14.75, 14.75, 1.8]);
               cube([13.75, 14.75, 0.01], center = true);
            }

            translate([-3.81, 2.54, -3.8]) cylinder(r = 1.75, h = 3.8, $fa = $middlePlateFa);
            translate([ 2.54, 5.08, -3.8]) cylinder(r = 1.75, h = 3.8, $fa = $middlePlateFa);

            translate([-5.08, 0, -3.8]) cylinder(r = 1.35, h = 3.8, $fa = $middlePlateFa);
            translate([ 0.00, 0, -3.8]) cylinder(r = 2.50, h = 3.8, $fa = $middlePlateFa);
            translate([ 5.08, 0, -3.8]) cylinder(r = 1.35, h = 3.8, $fa = $middlePlateFa);
         }
      }
   """)
}

fun ScadWriter.middlePlate() {
   fun ScadWriter.keySwitchHole() {
      writeRawScad("key_switch_hole();")
   }

   difference {
      cube(19.05.mm * 2, 19.05.mm * 2, 1.mm + 5.mm - 1.5.mm)
      translate(19.05.mm * 0.5, 19.05.mm * 0.5) { keySwitchHole() }
      translate(19.05.mm * 0.5, 19.05.mm * 1.5) { keySwitchHole() }
      translate(19.05.mm * 1.5, 19.05.mm * 0.5) { keySwitchHole() }
      translate(19.05.mm * 1.5, 19.05.mm * 1.5) { keySwitchHole() }
   }
}
