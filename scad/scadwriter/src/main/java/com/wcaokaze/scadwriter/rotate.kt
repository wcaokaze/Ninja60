package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.Angle
import com.wcaokaze.scadwriter.foundation.rad

inline fun ScadWriter.rotate(x: Angle = 0.0.rad,
                             y: Angle = 0.0.rad,
                             z: Angle = 0.0.rad,
                             children: ScadWriter.() -> Unit)
{
   writeBlock("rotate([$x, $y, $z])", children)
}
