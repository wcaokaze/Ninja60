package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

data class Sphere(
   val radius: Size,
   val fa: Double
) : ScadParentObject() {
   override fun writeScad(scadWriter: ScadWriter) {
      scadWriter.writeln("sphere($radius, \$fa = $fa);")
   }
}

fun ScadParentObject.sphere(radius: Size, fa: Double): Sphere {
   val sphere = Sphere(radius, fa)
   addChild(sphere)
   return sphere
}
