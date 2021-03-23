package com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation

import com.wcaokaze.ninja60.scadgenerator.scadwriter.ScadWriter
import com.wcaokaze.ninja60.scadgenerator.scadwriter.write
import com.wcaokaze.ninja60.scadgenerator.scadwriter.writeln

fun ScadWriter.writeRawScad(rawScad: String) {
   val indent = buildString {
      repeat (indent) {
         append("    ")
      }
   }

   write(rawScad.replaceIndent(indent))
   writeln()
}
