package com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation

import java.lang.StrictMath.*

inline class Angle(private val a: Double) : Comparable<Angle> {
   override fun toString() = toDegrees(a).toString()

   operator fun plus (another: Angle) = Angle(a + another.a)
   operator fun minus(another: Angle) = Angle(a - another.a)

   operator fun times(n: Int)    = Angle(a * n)
   operator fun times(n: Double) = Angle(a * n)
   operator fun div  (n: Int)    = Angle(a / n)
   operator fun div  (n: Double) = Angle(a / n)

   override operator fun compareTo(other: Angle): Int = a.compareTo(other.a)

   operator fun unaryMinus() = Angle(-a)
   operator fun unaryPlus () = Angle(+a)

   operator fun rangeTo(end: Angle) = AngleRange(this, end)
}

data class AngleRange(override val start: Angle,
                      override val endInclusive: Angle) : ClosedRange<Angle>

inline val Double.rad get() = Angle(this)

inline val Int   .deg get() = toRadians(this.toDouble()).rad
inline val Double.deg get() = toRadians(this).rad
