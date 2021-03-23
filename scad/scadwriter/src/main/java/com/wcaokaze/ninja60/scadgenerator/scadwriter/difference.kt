package com.wcaokaze.ninja60.scadgenerator.scadwriter

inline fun ScadWriter.difference(children: ScadWriter.() -> Unit) {
   writeBlock("difference()", children)
}
