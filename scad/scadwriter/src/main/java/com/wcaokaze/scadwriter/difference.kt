package com.wcaokaze.scadwriter

inline fun ScadWriter.difference(children: ScadWriter.() -> Unit) {
   writeBlock("difference()", children)
}
