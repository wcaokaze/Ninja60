package com.wcaokaze.ninja60.case.scad

import com.wcaokaze.ninja60.case.*
import com.wcaokaze.scadwriter.*

private val _generateWristRest = PropagatedValue { false }
val Case.Companion.generateWristRest get() = _generateWristRest

private val _generateBackRotaryEncoder = PropagatedValue { false }
val Case.Companion.generateBackRotaryEncoder get() = _generateBackRotaryEncoder
