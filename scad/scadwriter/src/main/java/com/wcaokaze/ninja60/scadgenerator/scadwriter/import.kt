package com.wcaokaze.ninja60.scadgenerator.scadwriter

import java.io.File

fun ScadWriter.import(file: File) {
   writeln("import(\"${file.absolutePath}\");")
}
