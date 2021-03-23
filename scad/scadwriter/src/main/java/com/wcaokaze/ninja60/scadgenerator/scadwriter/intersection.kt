package com.wcaokaze.ninja60.scadgenerator.scadwriter

inline fun ScadWriter.intersection(children: ScadWriter.() -> Unit) {
   writeBlock("intersection()", children)
}
