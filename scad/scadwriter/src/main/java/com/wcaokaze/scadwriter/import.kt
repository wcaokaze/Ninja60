package com.wcaokaze.scadwriter

import java.io.*

data class Import(
   override val parent: ScadParentObject,
   val file: File
) : ScadPrimitiveObject() {
   override fun toScadRepresentation() = "import(\"${file.absolutePath}\");"
}

fun ScadParentObject.import(file: File): Import {
   val import = Import(this, file)
   addChild(import)
   return import
}

data class Include(
   override val parent: ScadParentObject,
   val file: File
) : ScadPrimitiveObject() {
   override fun toScadRepresentation() = "include <${file.absolutePath}>;"
}

fun ScadParentObject.include(file: File): Include {
   val include = Include(this, file)
   addChild(include)
   return include
}

data class Use(
   override val parent: ScadParentObject,
   val file: File
) : ScadPrimitiveObject() {
   override fun toScadRepresentation() = "use <${file.absolutePath}>;"
}

fun ScadParentObject.use(file: File): Use {
   val use = Use(this, file)
   addHeader(use)
   return use
}
