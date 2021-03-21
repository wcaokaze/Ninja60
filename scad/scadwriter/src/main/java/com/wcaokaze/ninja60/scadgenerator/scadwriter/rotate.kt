package com.wcaokaze.ninja60.scadgenerator.scadwriter

import java.lang.StrictMath.*

data class Rotate(val x: Double, val y: Double, val z: Double) {
   override fun toString() = "[$x, $y, $z]"
}

inline fun ScadWriter.rotate(x: Double = 0.0,
                             y: Double = 0.0,
                             z: Double = 0.0,
                             children: ScadWriter.() -> Unit)
{
   rotate(Rotate(x, y, z), children)
}

inline fun ScadWriter.rotate(rotate: Rotate, children: ScadWriter.() -> Unit) {
   val x = toDegrees(rotate.x)
   val y = toDegrees(rotate.y)
   val z = toDegrees(rotate.z)

   writeBlock("rotate([$x, $y, $z])", children)
}
