package com.wcaokaze.ninja60.scadgenerator.scadwriter

fun ScadWriter.cylinder(height: Double, radius: Double, fa: Double) {
   writeln("cylinder(h = $height, r = $radius, \$fa = $fa);")
}

fun ScadWriter.cylinder(height: Double, bottomRadius: Double, topRadius: Double, fa: Double) {
   writeln("cylinder(h = $height, $bottomRadius, $topRadius, \$fa = $fa);")
}

fun ScadWriter.cylinder(height: Double, radius: Double, center: Boolean, fa: Double) {
   writeln("cylinder(h = $height, r = $radius, center = $center, \$fa = $fa);")
}

fun ScadWriter.cylinder(height: Double,
                        bottomRadius: Double, topRadius: Double,
                        center: Boolean,
                        fa: Double)
{
   writeln("cylinder(h = $height, $bottomRadius, $topRadius, center = $center, \$fa = fa);")
}
