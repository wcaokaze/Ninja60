package com.wcaokaze.scadwriter

import java.io.*

class TopLevelScadObject : ScadParentObject() {
   public override fun writeScad(scadWriter: ScadWriter) {
      for (c in children) {
         c.writeScad(scadWriter)
      }
   }
}

inline fun writeScad(outputFile: File, scad: TopLevelScadObject.() -> Unit) {
   outputFile.outputStream().bufferedWriter().use { writeScad(it, scad) }
}

inline fun writeScad(writer: Writer, scad: TopLevelScadObject.() -> Unit) {
   val topLevelScadObject = TopLevelScadObject()
   topLevelScadObject.scad()

   val scadWriter = ScadWriter(writer)
   topLevelScadObject.writeScad(scadWriter)
}
