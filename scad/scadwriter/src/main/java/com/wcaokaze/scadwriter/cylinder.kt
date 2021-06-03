package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.Size

fun ScadWriter.cylinder(height: Size, radius: Size, fa: Double) {
   writeln("cylinder(h = $height, r = $radius, \$fa = $fa);")
}

fun ScadWriter.cylinder(height: Size, bottomRadius: Size, topRadius: Size, fa: Double) {
   writeln("cylinder(h = $height, $bottomRadius, $topRadius, \$fa = $fa);")
}

fun ScadWriter.cylinder(height: Size, radius: Size, center: Boolean, fa: Double) {
   writeln("cylinder(h = $height, r = $radius, center = $center, \$fa = $fa);")
}

fun ScadWriter.cylinder(height: Size,
                        bottomRadius: Size, topRadius: Size,
                        center: Boolean,
                        fa: Double)
{
   writeln("cylinder(h = $height, $bottomRadius, $topRadius, center = $center, \$fa = fa);")
}
