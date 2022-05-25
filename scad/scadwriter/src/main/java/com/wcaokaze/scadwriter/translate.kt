package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*
import com.wcaokaze.scadwriter.linearalgebra.*

data class Translate(
   override val parent: ScadParentObject,
   val distance: Vector3d
) : ScadParentObject() {
   override fun toScadRepresentation()
         = buildChildrenScad("translate(${distance.scad})")
}

inline fun ScadParentObject.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm,
   children: Translate.() -> Unit
): Translate = translate(Vector3d(x, y, z), children)

inline fun ScadParentObject.translate(
   distance: Vector3d,
   children: Translate.() -> Unit
): Translate {
   val translate = Translate(this, distance)
   addChild(translate)
   translate.children()
   return translate
}

fun ScadParentObject.translate(
   direction: Vector3d,
   distance: Size,
   children: Translate.() -> Unit
): Translate {
   return translate(direction.toUnitVector() * distance.numberAsMilliMeter, children)
}
