package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

data class Text(
   override val parent: ScadParentObject,
   val text: String,
   val size: Size,
   val fontName: String,
   val hAlign: HAlign = HAlign.LEFT,
   val vAlign: VAlign = VAlign.BASELINE,
   val direction: Direction = Direction.LEFT_TO_RIGHT
) : ScadPrimitiveObject() {
   override fun toScadRepresentation(): String {
      return "text(\"$text\", size = $size, font = \"$fontName\", halign = \"$hAlign\"," +
            "valign = \"$vAlign\", direction = \"$direction\", \$fs = ${fs.value.scad});"
   }
}

enum class HAlign(private val string: String) {
   LEFT  ("left"),
   RIGHT ("right"),
   CENTER("center"),
   ;
   override fun toString() = string
}

enum class VAlign(private val string: String) {
   TOP     ("top"),
   BOTTOM  ("bottom"),
   BASELINE("baseline"),
   CENTER  ("center"),
   ;
   override fun toString() = string
}

enum class Direction(private val string: String) {
   LEFT_TO_RIGHT("ltr"),
   RIGHT_TO_LEFT("rtl"),
   TOP_TO_BOTTOM("ttb"),
   BOTTOM_TO_TOP("btt"),
   ;
   override fun toString() = string
}

fun ScadParentObject.text(
   text: String,
   size: Size,
   fontName: String,
   hAlign: HAlign = HAlign.LEFT,
   vAlign: VAlign = VAlign.BASELINE,
   direction: Direction = Direction.LEFT_TO_RIGHT
): Text {
   val text = Text(this, text, size, fontName, hAlign, vAlign, direction)
   addChild(text)
   return text
}
