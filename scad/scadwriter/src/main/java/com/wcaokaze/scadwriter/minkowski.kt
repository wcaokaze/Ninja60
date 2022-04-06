package com.wcaokaze.scadwriter

class Minkowski(override val parent: ScadParentObject) : ScadParentObject() {
   override fun toScadRepresentation() = buildChildrenScad("minkowski()")
}

inline fun ScadParentObject.minkowski(children: Minkowski.() -> Unit): Minkowski {
   val minkowski = Minkowski(this)
   addChild(minkowski)
   minkowski.children()
   return minkowski
}
