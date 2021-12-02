package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

data class Sphere(
   val radius: Size,
   val fa: Angle
) : ScadParentObject() {
   override fun writeScad(scadWriter: ScadWriter) {
      scadWriter.writeln("sphere($radius, \$fa = $fa);")
   }
}

fun ScadParentObject.sphere(radius: Size, fa: Angle): Sphere {
   val sphere = Sphere(radius, fa)
   addChild(sphere)
   return sphere
}
