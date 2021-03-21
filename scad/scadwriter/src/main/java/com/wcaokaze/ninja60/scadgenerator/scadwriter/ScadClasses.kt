package com.wcaokaze.ninja60.scadgenerator.scadwriter

data class Point2d(val x: Double, val y: Double) {
   override fun toString() = "[$x, $y]"
}

data class Point3d(val x: Double, val y: Double, val z: Double) {
   override fun toString() = "[$x, $y, $z]"
}

data class Size2d(val x: Double, val y: Double) {
   override fun toString() = "[$x, $y]"
}

data class Size3d(val x: Double, val y: Double, val z: Double) {
   override fun toString() = "[$x, $y, $z]"
}
