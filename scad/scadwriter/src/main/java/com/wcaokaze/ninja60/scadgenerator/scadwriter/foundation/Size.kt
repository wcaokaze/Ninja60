package com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation

inline class Size(private val s: Double) : Comparable<Size> {
   override fun toString() = s.toString()

   operator fun plus (another: Size) = Size(s + another.s)
   operator fun minus(another: Size) = Size(s - another.s)

   operator fun times(n: Int)    = Size(s * n)
   operator fun times(n: Double) = Size(s * n)
   operator fun div  (n: Int)    = Size(s / n)
   operator fun div  (n: Double) = Size(s / n)

   override operator fun compareTo(other: Size): Int = s.compareTo(other.s)

   operator fun unaryMinus() = Size(-s)
   operator fun unaryPlus () = Size(+s)

   operator fun rangeTo(end: Size) = SizeRange(this, end)
}

data class SizeRange(override val start: Size,
                     override val endInclusive: Size) : ClosedRange<Size>

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
