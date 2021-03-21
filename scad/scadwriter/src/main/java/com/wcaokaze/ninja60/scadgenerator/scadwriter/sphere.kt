package com.wcaokaze.ninja60.scadgenerator.scadwriter

import com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation.Size

fun ScadWriter.sphere(radius: Size, fa: Double) {
   writeln("sphere($radius, \$fa = $fa);")
}
