package com.wcaokaze.scadwriter

import java.io.*

class TopLevelScadObject : ScadParentObject() {
   private val headerObjects = ArrayList<ScadObject>()

   override fun addHeader(headerObject: ScadObject) {
      headerObjects += headerObject
   }

   override fun toScadRepresentation(): String {
      return buildString {
         for (h in headerObjects) {
            appendLine(h.toScadRepresentation())
         }

         appendLine(buildChildrenScad(""))
      }
   }
}

inline fun writeScad(outputFile: File, scad: TopLevelScadObject.() -> Unit) {
   outputFile.outputStream().bufferedWriter().use { writeScad(it, scad) }
}

inline fun writeScad(writer: Writer, scad: TopLevelScadObject.() -> Unit) {
   val topLevelScadObject = TopLevelScadObject()
   topLevelScadObject.scad()

   topLevelScadObject.writeScad(writer)
}
