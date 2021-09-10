package com.wcaokaze.scadwriter

class Difference : ScadParentObject() {
   override fun writeScad(scadWriter: ScadWriter) {
      writeChildren(scadWriter, "difference()")
   }
}

inline fun ScadParentObject.difference(children: Difference.() -> Unit): Difference {
   val difference = Difference()
   addChild(difference)
   difference.children()
   return difference
}
