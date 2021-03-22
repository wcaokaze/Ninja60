package com.wcaokaze.ninja60.scadgenerator

import com.wcaokaze.ninja60.scadgenerator.scadwriter.*
import com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation.*

val encoderKnobR = 11.mm
val encoderKnobH = 14.mm

fun ScadWriter.encoderKnob() {
   cylinder(height = encoderKnobH, radius = encoderKnobR, `$fa`)
}
