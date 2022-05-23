package com.wcaokaze.scadwriter.linearalgebra

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

fun Point3d.translate(distance: Size3d) = Point3d(
   x + distance.x,
   y + distance.y,
   z + distance.z
)

fun Point3d.translate(distance: Vector3d): Point3d
      = translate(Size3d(distance.x, distance.y, distance.z))

fun Point3d.translate(direction: Vector3d, distance: Size): Point3d
      = translate(direction.toUnitVector() * distance.numberAsMilliMeter)

fun Point3d.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm
): Point3d = translate(Size3d(x, y, z))

fun ScadParentObject.translate(
   distance: Vector3d,
   children: Translate.() -> Unit
): Translate {
   return translate(distance.x, distance.y, distance.z, children)
}

fun ScadParentObject.translate(
   direction: Vector3d,
   distance: Size,
   children: Translate.() -> Unit
): Translate {
   return translate(direction.toUnitVector() * distance.numberAsMilliMeter, children)
}
