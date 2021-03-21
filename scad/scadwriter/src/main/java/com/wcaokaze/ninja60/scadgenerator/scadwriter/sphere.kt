package com.wcaokaze.ninja60.scadgenerator.scadwriter

fun ScadWriter.sphere(radius: Double, fa: Double) {
   writeln("sphere($radius, \$fa = $fa);")
}
