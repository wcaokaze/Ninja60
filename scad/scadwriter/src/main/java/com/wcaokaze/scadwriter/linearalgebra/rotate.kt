package com.wcaokaze.scadwriter.linearalgebra

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

inline fun ScadParentObject.rotate(
   a: Angle,
   v: Vector3d,
   children: RotateWithAxis.() -> Unit
): RotateWithAxis = rotate(a, Point3d.ORIGIN.translate(v), children)
