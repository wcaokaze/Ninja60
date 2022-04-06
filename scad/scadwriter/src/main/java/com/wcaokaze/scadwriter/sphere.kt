package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

data class Sphere(
   override val parent: ScadParentObject,
   val radius: Size,
   val fa: Angle
) : ScadPrimitiveObject() {
   override fun toScadRepresentation()
         = "sphere(${radius.scad}, \$fa = ${fa.scad});"
}

fun ScadParentObject.sphere(radius: Size, fa: Angle): Sphere {
   val sphere = Sphere(this, radius, fa)
   addChild(sphere)
   return sphere
}
