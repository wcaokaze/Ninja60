package com.wcaokaze.ninja60.scadgenerator

import com.wcaokaze.ninja60.scadgenerator.scadwriter.*
import com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation.*

val bottomPlateFa = 5.0

fun ScadWriter.prepareBottomPlateModule() {
   val fa = "\$fa"
   writeRawScad("""
      module basic_key_hole() {
         union() {
            translate([-5, 0.55]) difference() {
               intersection() {
                  minkowski() {
                     cube([11.9 - 3, 6.89 - 3, 0.01]);
                     cylinder(r = 3, h = 2.35, $fa = $bottomPlateFa);
                  }

                  cube([11.9, 6.89, 2.35]);
               }

               minkowski() {
                  cube([11, 0.01, 0.01], center = true);
                  cylinder(r = 2.39, h = 5, $fa = $bottomPlateFa);
               }
            }

            translate([-8.5, 3.34]) cube([3.8, 4.1, 2.15]);
            translate([ 6.8, 0.55]) cube([3.8, 4.1, 2.15]);

            translate([-5.08, 0]) cylinder(r = 1.35, h = 1.7, $fa = $bottomPlateFa);
            translate([ 0.00, 0]) cylinder(r = 2.50, h = 1.7, $fa = $bottomPlateFa);
            translate([ 5.08, 0]) cylinder(r = 1.35, h = 1.7, $fa = $bottomPlateFa);

            difference() {
               linear_extrude(height = 1.7) {
                  polygon([[0, 0], [-2.06, 2.325], [-2.06, 3], [5.08, 3], [5.08, 0]]);
               }

               translate([ 3.115, -0.070]) cylinder(r = 0.615, h = 2.15, $fa = $bottomPlateFa);
               translate([-2.060,  2.325]) cylinder(r = 0.615, h = 2.15, $fa = $bottomPlateFa);
            }

            translate([0, -4.7, 0.9]) cube([6.6, 2.7, 1.8], center = true);
         }
      }
   """)
}

fun ScadWriter.bottomPlate() {
   fun ScadWriter.basicKeyHole() {
      writeRawScad("basic_key_hole();")
   }

   difference {
      cube(19.05.mm * 2, 19.05.mm * 2, 3.35.mm)
      translate(19.05.mm * 0.5, 19.05.mm * 0.5) { basicKeyHole() }
      translate(19.05.mm * 0.5, 19.05.mm * 1.5) { basicKeyHole() }
      translate(19.05.mm * 1.5, 19.05.mm * 0.5) { basicKeyHole() }
      translate(19.05.mm * 1.5, 19.05.mm * 1.5) { basicKeyHole() }
   }
}
