package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

data class Mirror(
   val normalVector: Size3d
) : ScadParentObject() {
   override fun writeScad(scadWriter: ScadWriter) {
      writeChildren(scadWriter, "mirror(${normalVector.scad})")
   }
}

inline fun ScadParentObject.mirror(
   x: Size,
   y: Size,
   z: Size,
   children: Mirror.() -> Unit
): Mirror = mirror(Size3d(x, y, z), children)

inline fun ScadParentObject.mirror(
   normalVector: Size3d,
   children: Mirror.() -> Unit
): Mirror {
   val mirror = Mirror(normalVector)
   addChild(mirror)
   mirror.children()
   return mirror
}
