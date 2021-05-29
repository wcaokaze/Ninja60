package com.wcaokaze.scadwriter

import java.io.File

fun ScadWriter.import(file: File) {
   writeln("import(\"${file.absolutePath}\");")
}

fun ScadWriter.include(file: File) {
   writeln("include <${file.absolutePath}>;")
}

fun ScadWriter.use(file: File) {
   writeln("use <${file.absolutePath}>;")
}
