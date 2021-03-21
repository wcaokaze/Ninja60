package com.wcaokaze.ninja60.scadgenerator.scadwriter

data class Translate(val x: Double, val y: Double, val z: Double) {
   override fun toString() = "[$x, $y, $z]"
}

inline fun ScadWriter.translate(x: Double = 0.0,
                                y: Double = 0.0,
                                z: Double = 0.0,
                                children: ScadWriter.() -> Unit)
{
   translate(Translate(x, y, z), children)
}

inline fun ScadWriter.translate(translate: Translate, children: ScadWriter.() -> Unit) {
   writeBlock("translate($translate)", children)
}
