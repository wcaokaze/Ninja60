package com.wcaokaze.scadwriter.foundation

inline class Size(val numberAsMilliMeter: Double) : Comparable<Size> {
   override fun toString() = numberAsMilliMeter.toString()

   operator fun plus (another: Size) = Size(numberAsMilliMeter + another.numberAsMilliMeter)
   operator fun minus(another: Size) = Size(numberAsMilliMeter - another.numberAsMilliMeter)

   operator fun times(n: Int)    = Size(numberAsMilliMeter * n)
   operator fun times(n: Double) = Size(numberAsMilliMeter * n)
   operator fun div  (n: Int)    = Size(numberAsMilliMeter / n)
   operator fun div  (n: Double) = Size(numberAsMilliMeter / n)

   override operator fun compareTo(other: Size): Int = numberAsMilliMeter.compareTo(other.numberAsMilliMeter)

   operator fun unaryMinus() = Size(-numberAsMilliMeter)
   operator fun unaryPlus () = Size(+numberAsMilliMeter)

   operator fun rangeTo(end: Size) = SizeRange(this, end)
}

data class SizeRange(override val start: Size,
                     override val endInclusive: Size) : ClosedRange<Size>
{
   infix fun step(step: Size) = Iterable {
      object : Iterator<Size> {
         private val precision = step / 2.0
         private var nextIndex = 0

         override fun hasNext() = if (step > 0.mm) {
            start + step * nextIndex <= endInclusive + precision
         } else {
            start + step * nextIndex >= endInclusive - precision
         }

         override fun next() = start + step * nextIndex++
      }
   }
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
