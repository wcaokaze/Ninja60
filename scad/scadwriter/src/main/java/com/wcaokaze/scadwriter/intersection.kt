package com.wcaokaze.scadwriter

class Intersection(override val parent: ScadParentObject) : ScadParentObject() {
   override fun toScadRepresentation() = buildChildrenScad("intersection()")
}

inline fun ScadParentObject.intersection(children: Intersection.() -> Unit): Intersection {
   val intersection = Intersection(this)
   addChild(intersection)
   intersection.children()
   return intersection
}
