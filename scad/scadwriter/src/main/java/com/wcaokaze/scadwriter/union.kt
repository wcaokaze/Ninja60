package com.wcaokaze.scadwriter

class Union : ScadParentObject() {
   override fun toScadRepresentation() = buildChildrenScad("union()")
}

inline fun ScadParentObject.union(children: Union.() -> Unit): Union {
   val union = Union()
   addChild(union)
   union.children()
   return union
}
