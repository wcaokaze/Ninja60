package com.wcaokaze.scadwriter

import java.io.*

class TopLevelScadObject : ScadParentObject() {
   private val headerObjects = ArrayList<ScadObject>()

   override val parent: ScadParentObject
      get() = throw NoSuchElementException("TopLevelScadObject has no parent.")

   override val propagatedValues: PropagatedValues
      get() = PropagatedValues()

   override fun addHeader(headerObject: ScadObject) {
      headerObjects += headerObject
   }

   override fun toScadRepresentation(): String {
      return buildString {
         for (h in headerObjects) {
            appendLine(h.toScadRepresentation())
         }

         append(buildChildrenScad(""))
      }
   }
}

inline fun writeScad(outputFile: File, scad: TopLevelScadObject.() -> Unit) {
   outputFile.outputStream().bufferedWriter().use { writeScad(it, scad) }
}

inline fun writeScad(writer: Writer, scad: TopLevelScadObject.() -> Unit) {
   val topLevelScadObject = TopLevelScadObject()
   topLevelScadObject.scad()

   writer.write(topLevelScadObject.toScadRepresentation() + '\n')
}
