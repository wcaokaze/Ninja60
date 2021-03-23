package com.wcaokaze.ninja60.scadgenerator.scadwriter

inline fun ScadWriter.union(children: ScadWriter.() -> Unit) {
   writeBlock("union()", children)
}
