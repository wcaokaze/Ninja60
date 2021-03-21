package com.wcaokaze.ninja60.scadgenerator.scadwriter

fun ScadWriter.cube(x: Double, y: Double, z: Double) {
   cube(Size3d(x, y, z))
}

fun ScadWriter.cube(x: Double, y: Double, z: Double, center: Boolean) {
   cube(Size3d(x, y, z), center)
}

fun ScadWriter.cube(size: Size3d) {
   writeln("cube($size);")
}

fun ScadWriter.cube(size: Size3d, center: Boolean = false) {
   writeln("cube($size, center = $center);")
}
