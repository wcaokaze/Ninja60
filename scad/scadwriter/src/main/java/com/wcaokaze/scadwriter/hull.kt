package com.wcaokaze.scadwriter

class Hull : ScadParentObject() {
   override fun toScadRepresentation() = buildChildrenScad("hull()")
}

inline fun ScadParentObject.hull(children: Hull.() -> Unit): Hull {
   val hull = Hull()
   addChild(hull)
   hull.children()
   return hull
}
