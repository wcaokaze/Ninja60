package com.wcaokaze.scadwriter

inline fun ScadWriter.intersection(children: ScadWriter.() -> Unit) {
   writeBlock("intersection()", children)
}
