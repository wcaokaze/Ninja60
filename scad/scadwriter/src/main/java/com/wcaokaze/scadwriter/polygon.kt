package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.Point2d

fun ScadWriter.polygon(points: List<Point2d>) {
   writeIndent()
   write("polygon(")
   writeArray(points)
   write(");")
   writeln()
}
