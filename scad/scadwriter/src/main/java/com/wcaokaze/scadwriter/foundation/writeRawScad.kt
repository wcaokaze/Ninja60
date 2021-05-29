package com.wcaokaze.scadwriter.foundation

import com.wcaokaze.scadwriter.ScadWriter
import com.wcaokaze.scadwriter.write
import com.wcaokaze.scadwriter.writeln

fun ScadWriter.writeRawScad(rawScad: String) {
   val indent = buildString {
      repeat (indent) {
         append("    ")
      }
   }

   write(rawScad.replaceIndent(indent))
   writeln()
}
