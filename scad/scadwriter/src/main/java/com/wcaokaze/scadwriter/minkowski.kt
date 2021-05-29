package com.wcaokaze.scadwriter

inline fun ScadWriter.minkowski(children: ScadWriter.() -> Unit) {
   writeBlock("minkowski()", children)
}
