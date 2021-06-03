package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.Size

inline fun ScadWriter.linearExtrude(height: Size, children: ScadWriter.() -> Unit) {
   writeBlock("linear_extrude($height)", children)
}

inline fun ScadWriter.linearExtrude(height: Size, center: Boolean,
                                    children: ScadWriter.() -> Unit)
{
   writeBlock("linear_extrude($height, center = $center)", children)
}
