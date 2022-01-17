package com.wcaokaze.scadwriter

class Intersection : ScadParentObject() {
   override fun toScadRepresentation() = buildChildrenScad("intersection()")
}

inline fun ScadParentObject.intersection(children: Intersection.() -> Unit): Intersection {
   val intersection = Intersection()
   addChild(intersection)
   intersection.children()
   return intersection
}
