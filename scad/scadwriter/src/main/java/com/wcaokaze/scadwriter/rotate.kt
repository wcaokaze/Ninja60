package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

inline fun ScadWriter.rotate(x: Angle = 0.0.rad,
                             y: Angle = 0.0.rad,
                             z: Angle = 0.0.rad,
                             children: ScadWriter.() -> Unit)
{
   writeBlock("rotate([$x, $y, $z])", children)
}

inline fun ScadWriter.rotate(a: Angle, v: Point3d, children: ScadWriter.() -> Unit)  {
   writeBlock("rotate($a, $v)", children)
}
