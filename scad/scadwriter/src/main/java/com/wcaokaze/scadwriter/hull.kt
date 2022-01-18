package com.wcaokaze.scadwriter

class Hull(private val parent: ScadParentObject) : ScadParentObject() {
   override fun addHeader(headerObject: ScadObject) {
      parent.addHeader(headerObject)
   }

   override fun toScadRepresentation() = buildChildrenScad("hull()")
}

inline fun ScadParentObject.hull(children: Hull.() -> Unit): Hull {
   val hull = Hull(this)
   addChild(hull)
   hull.children()
   return hull
}
