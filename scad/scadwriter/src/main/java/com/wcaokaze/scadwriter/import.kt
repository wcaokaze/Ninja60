package com.wcaokaze.scadwriter

import java.io.*

data class Import(
   val file: File
) : ScadPrimitiveObject() {
   override fun writeScad(scadWriter: ScadWriter) {
      scadWriter.writeln("import(\"${file.absolutePath}\");")
   }
}

fun ScadParentObject.import(file: File): Import {
   val import = Import(file)
   addChild(import)
   return import
}

data class Include(
   val file: File
) : ScadPrimitiveObject() {
   override fun writeScad(scadWriter: ScadWriter) {
      scadWriter.writeln("include <${file.absolutePath}>;")
   }
}

fun ScadParentObject.include(file: File): Include {
   val include = Include(file)
   addChild(include)
   return include
}

data class Use(
   val file: File
) : ScadPrimitiveObject() {
   override fun writeScad(scadWriter: ScadWriter) {
      scadWriter.writeln("use <${file.absolutePath}>;")
   }
}

fun ScadParentObject.use(file: File): Use {
   val use = Use(file)
   addChild(use)
   return use
}
