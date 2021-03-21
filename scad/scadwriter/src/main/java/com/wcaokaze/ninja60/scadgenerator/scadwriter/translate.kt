package com.wcaokaze.ninja60.scadgenerator.scadwriter

import com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation.Size
import com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation.Size3d
import com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation.mm

inline fun ScadWriter.translate(x: Size = 0.mm,
                                y: Size = 0.mm,
                                z: Size = 0.mm,
                                children: ScadWriter.() -> Unit)
{
   translate(Size3d(x, y, z), children)
}

inline fun ScadWriter.translate(distance: Size3d, children: ScadWriter.() -> Unit) {
   writeBlock("translate($distance)", children)
}
