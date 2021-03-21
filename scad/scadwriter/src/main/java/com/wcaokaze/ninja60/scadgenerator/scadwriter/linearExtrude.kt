package com.wcaokaze.ninja60.scadgenerator.scadwriter

inline fun ScadWriter.linearExtrude(height: Double, children: ScadWriter.() -> Unit) {
   writeBlock("linearExtrude($height)", children)
}

inline fun ScadWriter.linearExtrude(height: Double, center: Boolean,
                                    children: ScadWriter.() -> Unit)
{
   writeBlock("linearExtrude($height, center = $center)", children)
}
