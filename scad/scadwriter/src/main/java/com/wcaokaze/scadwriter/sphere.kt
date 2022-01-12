package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

data class Sphere(
   val radius: Size,
   val fa: Angle
) : ScadParentObject() {
   override fun writeScad(scadWriter: ScadWriter) {
      val radiusScad = radius.toScadRepresentation()
      val faScad = fa.toScadRepresentation()

      scadWriter.writeln("sphere($radiusScad, \$fa = $faScad);")
   }
}

fun ScadParentObject.sphere(radius: Size, fa: Angle): Sphere {
   val sphere = Sphere(radius, fa)
   addChild(sphere)
   return sphere
}
