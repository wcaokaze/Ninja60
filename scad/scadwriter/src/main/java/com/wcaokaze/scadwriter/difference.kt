package com.wcaokaze.scadwriter

class Difference(private val parent: ScadParentObject) : ScadParentObject() {
   override fun addHeader(headerObject: ScadObject) {
      parent.addHeader(headerObject)
   }

   override fun toScadRepresentation() = buildChildrenScad("difference()")
}

inline fun ScadParentObject.difference(children: Difference.() -> Unit): Difference {
   val difference = Difference(this)
   addChild(difference)
   difference.children()
   return difference
}
