package com.wcaokaze.ninja60

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

val encoderKnobR = 11.mm
val encoderKnobH = 14.mm

fun ScadWriter.encoderKnob() {
   cylinder(height = encoderKnobH, radius = encoderKnobR, `$fa`)
}
