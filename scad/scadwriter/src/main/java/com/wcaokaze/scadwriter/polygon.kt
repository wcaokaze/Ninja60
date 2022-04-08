package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

data class Polygon(
   override val parent: ScadParentObject,
   val points: List<Point2d>
) : ScadPrimitiveObject() {
   override fun toScadRepresentation() = "polygon(${buildScadArray(points)}, \$fs = ${fs.value.scad});"
}

fun ScadParentObject.polygon(points: List<Point2d>): Polygon {
   val polygon = Polygon(this, points)
   addChild(polygon)
   return polygon
}
