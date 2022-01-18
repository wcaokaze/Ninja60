package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

data class Sphere(
   val radius: Size,
   val fa: Angle
) : ScadPrimitiveObject() {
   override fun toScadRepresentation()
         = "sphere(${radius.scad}, \$fa = ${fa.scad});"
}

fun ScadParentObject.sphere(radius: Size, fa: Angle): Sphere {
   val sphere = Sphere(radius, fa)
   addChild(sphere)
   return sphere
}
