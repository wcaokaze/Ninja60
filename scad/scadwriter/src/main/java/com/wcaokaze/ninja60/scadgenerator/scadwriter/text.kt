package com.wcaokaze.ninja60.scadgenerator.scadwriter

import com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation.Size

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

fun ScadWriter.text(
   text: String,
   size: Size,
   fontName: String,
   hAlign: HAlign = HAlign.LEFT,
   vAlign: VAlign = VAlign.BASELINE,
   direction: Direction = Direction.LEFT_TO_RIGHT
) {
   writeln("text(\"$text\", size = $size, font = \"$fontName\", halign = \"$hAlign\"," +
           "valign = \"$vAlign\", direction = \"$direction\");")
}
