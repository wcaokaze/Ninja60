package com.wcaokaze.scadwriter

inline fun ScadWriter.union(children: ScadWriter.() -> Unit) {
   writeBlock("union()", children)
}
