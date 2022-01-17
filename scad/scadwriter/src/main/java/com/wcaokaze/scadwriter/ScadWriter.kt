package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*
import java.io.*

class ScadWriter(@PublishedApi internal val writer: Writer) {
   @PublishedApi
   internal var indent = 0
}

@PublishedApi
internal fun ScadWriter.write(str: String) {
   writer.write(str)
}

@PublishedApi
internal fun ScadWriter.writeIndent() {
   repeat (indent) {
      write("    ")
   }
}

@PublishedApi
internal fun ScadWriter.writeln() {
   write(System.lineSeparator())
}

@PublishedApi
internal fun ScadWriter.writeln(str: String) {
   writeIndent()
   write(str)
   writeln()
}

@PublishedApi
internal fun ScadWriter.writeArray(array: List<ScadValue>) {
   write("[")
   writeln()
   indent++

   for ((i, element) in array.withIndex()) {
      writeIndent()
      write(element.toScadRepresentation())

      if (i < array.lastIndex) {
         write(",")
      }

      writeln()
   }

   indent--
   writeIndent()
   write("]")
}

@PublishedApi
internal inline fun ScadWriter.writeBlock(str: String, block: ScadWriter.() -> Unit) {
   writeIndent()
   write(str)
   write(" {")
   writeln()
   indent++

   try {
      block()
   } finally {
      indent--
      writeln("}")
   }
}
