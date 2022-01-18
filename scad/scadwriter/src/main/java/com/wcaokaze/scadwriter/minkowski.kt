package com.wcaokaze.scadwriter

class Minkowski(private val parent: ScadParentObject) : ScadParentObject() {
   override fun addHeader(headerObject: ScadObject) {
      parent.addHeader(headerObject)
   }

   override fun toScadRepresentation() = buildChildrenScad("minkowski()")
}

inline fun ScadParentObject.minkowski(children: Minkowski.() -> Unit): Minkowski {
   val minkowski = Minkowski(this)
   addChild(minkowski)
   minkowski.children()
   return minkowski
}
