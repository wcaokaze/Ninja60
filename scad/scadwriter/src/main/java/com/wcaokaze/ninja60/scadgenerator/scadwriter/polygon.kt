package com.wcaokaze.ninja60.scadgenerator.scadwriter

fun ScadWriter.polygon(points: List<Point2d>) {
   writeIndent()
   write("polygon(")
   writeArray(points)
   write(");")
   writeln()
}
