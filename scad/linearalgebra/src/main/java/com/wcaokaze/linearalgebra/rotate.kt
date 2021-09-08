package com.wcaokaze.linearalgebra

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

fun Point3d.rotate(axis: Line3d, angle: Angle): Point3d {
   val ax = axis.vector.x.numberAsMilliMeter
   val ay = axis.vector.y.numberAsMilliMeter
   val az = axis.vector.z.numberAsMilliMeter

   val (px, py, pz) = axis.somePoint

   val tx = x - px
   val ty = y - py
   val tz = z - pz

   return Point3d(
      x = px + tx * (cos(angle) + ax * ax * (1.0 - cos(angle)))
             + ty * (ax * ay * (1.0 - cos(angle)) - az * sin(angle))
             + tz * (az * ax * (1.0 - cos(angle)) + ay * sin(angle)),

      y = py + tx * (ax * ay * (1.0 - cos(angle)) + az * sin(angle))
             + ty * (cos(angle) + ay * ay * (1.0 - cos(angle)))
             + tz * (ay * az * (1.0 - cos(angle)) - ax * sin(angle)),

      z = pz + tx * (az * ax * (1.0 - cos(angle)) - ay * sin(angle))
             + ty * (ay * az * (1.0 - cos(angle)) + ax * sin(angle))
             + tz * (cos(angle) + az * az * (1.0 - cos(angle)))
   )
}

inline fun ScadParentObject.rotate(
   a: Angle,
   v: Vector3d,
   children: RotateWithAxis.() -> Unit
): RotateWithAxis = rotate(a, Point3d.ORIGIN.translate(v), children)
