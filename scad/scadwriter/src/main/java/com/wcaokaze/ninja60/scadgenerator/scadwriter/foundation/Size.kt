package com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation

inline class Size(private val s: Double) {
   override fun toString() = s.toString()
}

inline val Int   .mm get() = Size(this.toDouble())
inline val Double.mm get() = Size(this)

inline val Int   .cm get() = (this * 10).mm
inline val Double.cm get() = (this * 10).mm

data class Size2d(val x: Size, val y: Size) {
   override fun toString() = "[$x, $y]"
}

data class Size3d(val x: Size, val y: Size, val z: Size) {
   override fun toString() = "[$x, $y, $z]"
}
