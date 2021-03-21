package com.wcaokaze.ninja60.scadgenerator.scadwriter

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
   writeBlock("rotate($rotate)", children)
}
