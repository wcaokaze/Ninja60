package com.wcaokaze.scadwriter.foundation

data class Size(val numberAsMilliMeter: Double)
   : ScadValue(), Comparable<Size>
{
   override fun toString() = "%.3fmm".format(numberAsMilliMeter)
   override fun toScadRepresentation() = numberAsMilliMeter.toString()

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
   override fun toString() = "$start..$endInclusive"

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

data class Size2d(val x: Size, val y: Size) : ScadValue() {
   override fun toString() = "($x, $y)"

   override fun toScadRepresentation()
         = "[${x.toScadRepresentation()}, ${y.toScadRepresentation()}]"

   operator fun plus (another: Size2d) = Size2d(x + another.x, y + another.y)
   operator fun minus(another: Size2d) = Size2d(x - another.x, y - another.y)

   operator fun times(n: Int)    = Size2d(x * n, y * n)
   operator fun times(n: Double) = Size2d(x * n, y * n)
   operator fun div  (n: Int)    = Size2d(x / n, y / n)
   operator fun div  (n: Double) = Size2d(x / n, y / n)

   operator fun unaryMinus() = Size2d(-x, -y)
   operator fun unaryPlus () = Size2d(+x, +y)
}

data class Size3d(val x: Size, val y: Size, val z: Size) : ScadValue() {
   override fun toString() = "($x, $y, $z)"

   override fun toScadRepresentation()
         = "[${x.toScadRepresentation()}, ${y.toScadRepresentation()}, ${z.toScadRepresentation()}]"

   operator fun plus (another: Size3d) = Size3d(x + another.x, y + another.y, z + another.z)
   operator fun minus(another: Size3d) = Size3d(x - another.x, y - another.y, z - another.z)

   operator fun times(n: Int)    = Size3d(x * n, y * n, z * n)
   operator fun times(n: Double) = Size3d(x * n, y * n, z * n)
   operator fun div  (n: Int)    = Size3d(x / n, y / n, z / n)
   operator fun div  (n: Double) = Size3d(x / n, y / n, z / n)

   operator fun unaryMinus() = Size3d(-x, -y, -z)
   operator fun unaryPlus () = Size3d(+x, +y, +z)
}

fun Iterable<Size>.sum() = Size(sumByDouble { it.numberAsMilliMeter })
fun Iterable<Size>.average() = Size(map { it.numberAsMilliMeter } .average())
