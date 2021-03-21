package com.wcaokaze.ninja60.scadgenerator.scadwriter

import com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation.Point2d

fun ScadWriter.polygon(points: List<Point2d>) {
   writeIndent()
   write("polygon(")
   writeArray(points)
   write(");")
   writeln()
}
