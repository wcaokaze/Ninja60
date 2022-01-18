package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

data class Rotate(
   private val parent: ScadParentObject,
   val x: Angle = 0.0.rad,
   val y: Angle = 0.0.rad,
   val z: Angle = 0.0.rad
) : ScadParentObject() {
   override fun addHeader(headerObject: ScadObject) {
      parent.addHeader(headerObject)
   }

   override fun toScadRepresentation()
         = buildChildrenScad("rotate([${x.scad}, ${y.scad}, ${z.scad}])")
}

inline fun ScadParentObject.rotate(
   x: Angle = 0.0.rad,
   y: Angle = 0.0.rad,
   z: Angle = 0.0.rad,
   children: Rotate.() -> Unit
): Rotate {
   val rotate = Rotate(this, x, y, z)
   addChild(rotate)
   rotate.children()
   return rotate
}

data class RotateWithAxis(
   private val parent: ScadParentObject,
   val a: Angle,
   val v: Point3d
) : ScadParentObject() {
   override fun addHeader(headerObject: ScadObject) {
      parent.addHeader(headerObject)
   }

   override fun toScadRepresentation()
         = buildChildrenScad("rotate(${a.scad}, ${v.scad})")
}

inline fun ScadParentObject.rotate(
   a: Angle,
   v: Point3d,
   children: RotateWithAxis.() -> Unit
): RotateWithAxis {
   val rotateWithAxis = RotateWithAxis(this, a, v)
   addChild(rotateWithAxis)
   rotateWithAxis.children()
   return rotateWithAxis
}
