package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

data class Translate(
   val distance: Size3d
) : ScadParentObject() {
   constructor(
      x: Size = 0.mm,
      y: Size = 0.mm,
      z: Size = 0.mm
   ) : this(Size3d(x, y, z))

   override fun toScadRepresentation()
         = buildChildrenScad("translate(${distance.scad})")
}

inline fun ScadParentObject.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm,
   children: Translate.() -> Unit
): Translate = translate(Size3d(x, y, z), children)

inline fun ScadParentObject.translate(
   distance: Size3d,
   children: Translate.() -> Unit
): Translate {
   val translate = Translate(distance)
   addChild(translate)
   translate.children()
   return translate
}

inline fun ScadParentObject.locale(
   x: Point = Point.ORIGIN,
   y: Point = Point.ORIGIN,
   z: Point = Point.ORIGIN,
   children: Translate.() -> Unit
): Translate = locale(Point3d(x, y, z), children)

inline fun ScadParentObject.locale(
   point: Point3d,
   children: Translate.() -> Unit
): Translate = translate(point - Point3d.ORIGIN, children)
