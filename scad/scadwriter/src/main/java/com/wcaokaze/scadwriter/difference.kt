package com.wcaokaze.scadwriter

class Difference(override val parent: ScadParentObject) : ScadParentObject() {
   override fun toScadRepresentation() = buildChildrenScad("difference()")
}

inline fun ScadParentObject.difference(children: Difference.() -> Unit): Difference {
   val difference = Difference(this)
   addChild(difference)
   difference.children()
   return difference
}
