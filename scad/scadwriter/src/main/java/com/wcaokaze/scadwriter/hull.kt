package com.wcaokaze.scadwriter

inline fun ScadWriter.hull(children: ScadWriter.() -> Unit) {
   writeBlock("hull()", children)
}
