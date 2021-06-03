package com.wcaokaze.scadwriter

import java.io.File
import java.io.Writer

class ScadWriter(@PublishedApi internal val writer: Writer) {
   @PublishedApi
   internal var indent = 0

   var `$fs`: Double = 2.0
      set(value) {
         field = value
         writeln("\$fs = $value;")
      }

   var `$fa`: Double = 12.0
      set(value) {
         field = value
         writeln("\$fa = $value;")
      }

   var `$fn`: Double = 0.0
      set(value) {
         field = value
         writeln("\$fn = $value;")
      }
}

inline fun writeScad(outputFile: File, scad: ScadWriter.() -> Unit) {
   outputFile.outputStream().bufferedWriter().use { writeScad(it, scad) }
}

inline fun writeScad(writer: Writer, scad: ScadWriter.() -> Unit) {
   ScadWriter(writer).scad()
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
internal fun ScadWriter.writeArray(array: List<Any?>) {
   write("[")
   writeln()
   indent++

   for ((i, element) in array.withIndex()) {
      writeIndent()
      write(element.toString())

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
