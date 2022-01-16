package com.wcaokaze.scadwriter.foundation

import com.wcaokaze.scadwriter.*

/**
 * OpenSCADのコードとして出力可能な値。
 *
 * 主に数値リテラルとして出力される。
 * `cube` などmoduleは[ScadObject]。
 */
sealed class ScadPrimitiveValue {
   internal abstract fun toScadRepresentation(): String

   internal open fun writeScad(scadWriter: ScadWriter) {
      scadWriter.write(toScadRepresentation())
   }
}

internal val ScadPrimitiveValue.scad: String
   get() = toScadRepresentation()
