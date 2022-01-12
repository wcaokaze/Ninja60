package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

data class Cube(
   val size: Size3d,
   val center: Boolean = false
) : ScadPrimitiveObject() {
   constructor(x: Size, y: Size, z: Size, center: Boolean = false)
         : this(Size3d(x, y, z), center)

   override fun writeScad(scadWriter: ScadWriter) {
      val sizeScad = size.toScadRepresentation()
      scadWriter.writeln("cube($sizeScad, center = $center);")
   }
}

fun ScadParentObject.cube(x: Size, y: Size, z: Size, center: Boolean = false): Cube
      = cube(Size3d(x, y, z), center)

fun ScadParentObject.cube(size: Size3d, center: Boolean = false): Cube {
   val cube = Cube(size, center)
   addChild(cube)
   return cube
}
