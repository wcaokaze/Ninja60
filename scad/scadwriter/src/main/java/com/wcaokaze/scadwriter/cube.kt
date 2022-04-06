package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

data class Cube(
   override val parent: ScadParentObject,
   val size: Size3d,
   val center: Boolean = false
) : ScadPrimitiveObject() {
   constructor(parent: ScadParentObject, x: Size, y: Size, z: Size, center: Boolean = false)
         : this(parent, Size3d(x, y, z), center)

   override fun toScadRepresentation() = "cube(${size.scad}, center = $center);"
}

fun ScadParentObject.cube(x: Size, y: Size, z: Size, center: Boolean = false): Cube
      = cube(Size3d(x, y, z), center)

fun ScadParentObject.cube(size: Size3d, center: Boolean = false): Cube {
   val cube = Cube(this, size, center)
   addChild(cube)
   return cube
}
