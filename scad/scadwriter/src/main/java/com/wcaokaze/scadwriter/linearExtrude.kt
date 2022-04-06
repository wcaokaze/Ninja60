package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

data class LinearExtrude(
   override val parent: ScadParentObject,
   val height: Size,
   val center: Boolean = false
) : ScadParentObject() {
   override fun toScadRepresentation()
         = buildChildrenScad("linear_extrude(${height.scad}, center = $center)")
}

inline fun ScadParentObject.linearExtrude(
   height: Size,
   center: Boolean = false,
   children: LinearExtrude.() -> Unit
): LinearExtrude {
   val linearExtrude = LinearExtrude(this, height, center)
   addChild(linearExtrude)
   linearExtrude.children()
   return linearExtrude
}
