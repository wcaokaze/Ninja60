package com.wcaokaze.scadwriter.foundation

import java.lang.StrictMath.*

data class Angle(val numberAsRadian: Double)
   : ScadPrimitiveValue(), Comparable<Angle>
{
   companion object {
      val PI: Angle = StrictMath.PI.rad
   }

   override fun toString() = "%.3f°".format(toDegrees(numberAsRadian))
   override fun toScadRepresentation() = toDegrees(numberAsRadian).toString()

   operator fun plus (another: Angle) = Angle(numberAsRadian + another.numberAsRadian)
   operator fun minus(another: Angle) = Angle(numberAsRadian - another.numberAsRadian)

   operator fun times(n: Int)    = Angle(numberAsRadian * n)
   operator fun times(n: Double) = Angle(numberAsRadian * n)
   operator fun div  (n: Int)    = Angle(numberAsRadian / n)
   operator fun div  (n: Double) = Angle(numberAsRadian / n)

   override operator fun compareTo(other: Angle): Int = numberAsRadian.compareTo(other.numberAsRadian)

   operator fun unaryMinus() = Angle(-numberAsRadian)
   operator fun unaryPlus () = Angle(+numberAsRadian)

   operator fun rangeTo(end: Angle) = AngleRange(this, end)
}

data class AngleRange(override val start: Angle,
                      override val endInclusive: Angle) : ClosedRange<Angle>
{
   override fun toString() = "$start..$endInclusive"

   infix fun step(step: Angle) = Iterable {
      object : Iterator<Angle> {
         private val precision = step / 2.0
         private var nextIndex = 0

         override fun hasNext() = if (step > 0.0.rad) {
            start + step * nextIndex <= endInclusive + precision
         } else {
            start + step * nextIndex >= endInclusive - precision
         }

         override fun next() = start + step * nextIndex++
      }
   }
}

inline val Double.rad get() = Angle(this)

inline val Int   .deg get() = toRadians(this.toDouble()).rad
inline val Double.deg get() = toRadians(this).rad

fun sin(a: Angle): Double = sin(a.numberAsRadian)
fun cos(a: Angle): Double = cos(a.numberAsRadian)
fun tan(a: Angle): Double = tan(a.numberAsRadian)

fun asin(y: Size, r: Size) = Angle(asin(y.numberAsMilliMeter / r.numberAsMilliMeter))
fun acos(x: Size, r: Size) = Angle(acos(x.numberAsMilliMeter / r.numberAsMilliMeter))
fun atan(y: Size, x: Size) = Angle(atan2(y.numberAsMilliMeter, x.numberAsMilliMeter))
