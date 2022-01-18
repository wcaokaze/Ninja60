package com.wcaokaze.scadwriter

class Union(private val parent: ScadParentObject) : ScadParentObject() {
   override fun addHeader(headerObject: ScadObject) {
      parent.addHeader(headerObject)
   }

   override fun toScadRepresentation() = buildChildrenScad("union()")
}

inline fun ScadParentObject.union(children: Union.() -> Unit): Union {
   val union = Union(this)
   addChild(union)
   union.children()
   return union
}
