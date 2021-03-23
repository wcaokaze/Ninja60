package com.wcaokaze.ninja60.scadgenerator.scadwriter

inline fun ScadWriter.minkowski(children: ScadWriter.() -> Unit) {
   writeBlock("minkowski()", children)
}
