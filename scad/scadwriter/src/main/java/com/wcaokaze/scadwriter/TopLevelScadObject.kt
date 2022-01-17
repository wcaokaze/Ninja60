package com.wcaokaze.scadwriter

import java.io.*

class TopLevelScadObject : ScadParentObject() {
   override fun toScadRepresentation() = buildChildrenScad("")
}

inline fun writeScad(outputFile: File, scad: TopLevelScadObject.() -> Unit) {
   outputFile.outputStream().bufferedWriter().use { writeScad(it, scad) }
}

inline fun writeScad(writer: Writer, scad: TopLevelScadObject.() -> Unit) {
   val topLevelScadObject = TopLevelScadObject()
   topLevelScadObject.scad()

   topLevelScadObject.writeScad(writer)
}
