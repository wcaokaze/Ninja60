package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

data class Polygon(
   val points: List<Point2d>
) : ScadPrimitiveObject() {
   override fun writeScad(scadWriter: ScadWriter) {
      scadWriter.writeIndent()
      scadWriter.write("polygon(")
      scadWriter.writeArray(points)
      scadWriter.write(");")
      scadWriter.writeln()
   }
}

fun ScadParentObject.polygon(points: List<Point2d>): Polygon {
   val polygon = Polygon(points)
   addChild(polygon)
   return polygon
}
