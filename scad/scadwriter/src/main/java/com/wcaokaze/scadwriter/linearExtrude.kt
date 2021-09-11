package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

data class LinearExtrude(
   val height: Size,
   val center: Boolean = false
) : ScadParentObject() {
   override fun writeScad(scadWriter: ScadWriter) {
      writeChildren(scadWriter, "linear_extrude($height, center = $center)")
   }
}

inline fun ScadParentObject.linearExtrude(
   height: Size,
   center: Boolean = false,
   children: LinearExtrude.() -> Unit
): LinearExtrude {
   val linearExtrude = LinearExtrude(height, center)
   addChild(linearExtrude)
   linearExtrude.children()
   return linearExtrude
}
