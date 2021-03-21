package com.wcaokaze.ninja60.scadgenerator.scadwriter

import com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation.Size
import com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation.Size3d

fun ScadWriter.cube(x: Size, y: Size, z: Size) {
   cube(Size3d(x, y, z))
}

fun ScadWriter.cube(x: Size, y: Size, z: Size, center: Boolean) {
   cube(Size3d(x, y, z), center)
}

fun ScadWriter.cube(size: Size3d) {
   writeln("cube($size);")
}

fun ScadWriter.cube(size: Size3d, center: Boolean = false) {
   writeln("cube($size, center = $center);")
}
