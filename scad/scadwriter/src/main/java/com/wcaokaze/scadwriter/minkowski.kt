package com.wcaokaze.scadwriter

class Minkowski : ScadParentObject() {
   override fun toScadRepresentation() = buildChildrenScad("minkowski()")
}

inline fun ScadParentObject.minkowski(children: Minkowski.() -> Unit): Minkowski {
   val minkowski = Minkowski()
   addChild(minkowski)
   minkowski.children()
   return minkowski
}
