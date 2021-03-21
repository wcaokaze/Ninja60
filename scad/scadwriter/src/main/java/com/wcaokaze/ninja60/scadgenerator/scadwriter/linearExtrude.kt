package com.wcaokaze.ninja60.scadgenerator.scadwriter

inline fun ScadWriter.linearExtrude(height: Double, children: ScadWriter.() -> Unit) {
   writeBlock("linear_extrude($height)", children)
}

inline fun ScadWriter.linearExtrude(height: Double, center: Boolean,
                                    children: ScadWriter.() -> Unit)
{
   writeBlock("linear_extrude($height, center = $center)", children)
}
