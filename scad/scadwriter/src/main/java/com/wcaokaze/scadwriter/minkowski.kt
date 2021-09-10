package com.wcaokaze.scadwriter

class Minkowski : ScadParentObject() {
   override fun writeScad(scadWriter: ScadWriter) {
      writeChildren(scadWriter, "minkowski()")
   }
}

inline fun ScadParentObject.minkowski(children: Minkowski.() -> Unit): Minkowski {
   val minkowski = Minkowski()
   addChild(minkowski)
   minkowski.children()
   return minkowski
}
