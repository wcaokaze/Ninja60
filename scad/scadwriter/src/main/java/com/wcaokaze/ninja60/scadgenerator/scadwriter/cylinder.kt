package com.wcaokaze.ninja60.scadgenerator.scadwriter

fun ScadWriter.cylinder(height: Double, radius: Double, fa: Double) {
   writeln("cylinder($height, $radius, \$fa = $fa);")
}

fun ScadWriter.cylinder(height: Double, bottomRadius: Double, topRadius: Double, fa: Double) {
   writeln("cylinder($height, $bottomRadius, $topRadius, \$fa = $fa);")
}

fun ScadWriter.cylinder(height: Double, radius: Double, center: Boolean, fa: Double) {
   writeln("cylinder($height, $radius, center = $center, \$fa = $fa);")
}

fun ScadWriter.cylinder(height: Double,
                        bottomRadius: Double, topRadius: Double,
                        center: Boolean,
                        fa: Double)
{
   writeln("cylinder($height, $bottomRadius, $topRadius, center = $center, \$fa = fa);")
}
