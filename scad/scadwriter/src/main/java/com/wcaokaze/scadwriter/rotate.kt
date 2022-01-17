package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

data class Rotate(
   val x: Angle = 0.0.rad,
   val y: Angle = 0.0.rad,
   val z: Angle = 0.0.rad
) : ScadParentObject() {
   override fun toScadRepresentation()
         = buildChildrenScad("rotate([${x.scad}, ${y.scad}, ${z.scad}])")
}

inline fun ScadParentObject.rotate(
   x: Angle = 0.0.rad,
   y: Angle = 0.0.rad,
   z: Angle = 0.0.rad,
   children: Rotate.() -> Unit
): Rotate {
   val rotate = Rotate(x, y, z)
   addChild(rotate)
   rotate.children()
   return rotate
}

data class RotateWithAxis(
   val a: Angle,
   val v: Point3d
) : ScadParentObject() {
   override fun toScadRepresentation()
         = buildChildrenScad("rotate(${a.scad}, ${v.scad})")
}

inline fun ScadParentObject.rotate(
   a: Angle,
   v: Point3d,
   children: RotateWithAxis.() -> Unit
): RotateWithAxis {
   val rotateWithAxis = RotateWithAxis(a, v)
   addChild(rotateWithAxis)
   rotateWithAxis.children()
   return rotateWithAxis
}
