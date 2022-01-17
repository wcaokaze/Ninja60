package com.wcaokaze.scadwriter

class Difference : ScadParentObject() {
   override fun toScadRepresentation() = buildChildrenScad("difference()")
}

inline fun ScadParentObject.difference(children: Difference.() -> Unit): Difference {
   val difference = Difference()
   addChild(difference)
   difference.children()
   return difference
}
