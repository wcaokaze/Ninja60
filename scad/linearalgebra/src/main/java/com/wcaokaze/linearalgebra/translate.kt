package com.wcaokaze.linearalgebra

import com.wcaokaze.scadwriter.foundation.*

fun Point3d.translate(distance: Size3d) = Point3d(
   x + distance.x,
   y + distance.y,
   z + distance.z
)

fun Point3d.translate(distance: Vector3d): Point3d
      = translate(Size3d(distance.x, distance.y, distance.z))

fun Point3d.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm
): Point3d = translate(Size3d(x, y, z))
