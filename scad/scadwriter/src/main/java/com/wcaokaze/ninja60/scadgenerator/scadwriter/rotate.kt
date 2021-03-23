package com.wcaokaze.ninja60.scadgenerator.scadwriter

import com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation.Angle
import com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation.rad

inline fun ScadWriter.rotate(x: Angle = 0.0.rad,
                             y: Angle = 0.0.rad,
                             z: Angle = 0.0.rad,
                             children: ScadWriter.() -> Unit)
{
   writeBlock("rotate([$x, $y, $z])", children)
}
