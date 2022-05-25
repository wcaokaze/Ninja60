package com.wcaokaze.scadwriter.linearalgebra

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

fun ScadParentObject.translate(
   distance: Vector3d,
   children: Translate.() -> Unit
): Translate {
   return translate(distance.x, distance.y, distance.z, children)
}

fun ScadParentObject.translate(
   direction: Vector3d,
   distance: Size,
   children: Translate.() -> Unit
): Translate {
   return translate(direction.toUnitVector() * distance.numberAsMilliMeter, children)
}
