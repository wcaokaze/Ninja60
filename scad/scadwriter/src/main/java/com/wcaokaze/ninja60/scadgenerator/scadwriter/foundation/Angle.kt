package com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation

import java.lang.StrictMath.*

inline class Angle(private val a: Double) {
   override fun toString() = toDegrees(a).toString()
}

inline val Double.rad get() = Angle(this)

inline val Int   .deg get() = toRadians(this.toDouble()).rad
inline val Double.deg get() = toRadians(this).rad
