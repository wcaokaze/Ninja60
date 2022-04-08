package com.wcaokaze.scadwriter.foundation

import com.wcaokaze.scadwriter.*

/**
 * OpenSCADのコードとして出力可能な値。
 */
abstract class ScadValue {
   abstract fun toScadRepresentation(): String

   protected fun buildScadBlock(content: List<ScadObject>): String {
      return "{\n" +
            content
               .joinToString("\n") { it.toScadRepresentation() }
               .prependIndent("  ") +
            "\n}"
   }

   protected fun buildScadArray(content: List<ScadValue>): String {
      return "[\n" +
            content
               .joinToString(",\n") { it.toScadRepresentation() }
               .prependIndent("  ") +
            "\n]"
   }
}

internal val ScadValue.scad: String
   get() = toScadRepresentation()
