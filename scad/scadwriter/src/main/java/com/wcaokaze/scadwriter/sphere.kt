package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

data class Sphere(
   override val parent: ScadParentObject,
   val radius: Size
) : ScadPrimitiveObject() {
   override fun toScadRepresentation()
         = "sphere(${radius.scad}, \$fa = ${fa.value.scad});"
}

fun ScadParentObject.sphere(radius: Size): Sphere {
   val sphere = Sphere(this, radius)
   addChild(sphere)
   return sphere
}
