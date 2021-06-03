package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.Size

fun ScadWriter.sphere(radius: Size, fa: Double) {
   writeln("sphere($radius, \$fa = $fa);")
}
