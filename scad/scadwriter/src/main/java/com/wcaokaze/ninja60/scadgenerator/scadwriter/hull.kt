package com.wcaokaze.ninja60.scadgenerator.scadwriter

inline fun ScadWriter.hull(children: ScadWriter.() -> Unit) {
   writeBlock("hull()", children)
}
