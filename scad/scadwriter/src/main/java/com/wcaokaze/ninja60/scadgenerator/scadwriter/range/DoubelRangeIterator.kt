package com.wcaokaze.ninja60.scadgenerator.scadwriter.range

infix fun ClosedFloatingPointRange<Double>.step(step: Double) = Iterable {
   object : Iterator<Double> {
      private val precision = step / 2.0
      private var nextIndex = 0

      override fun hasNext() = if (step > 0) {
         start + nextIndex * step <= endInclusive + precision
      } else {
         start + nextIndex * step >= endInclusive - precision
      }

      override fun next() = start + nextIndex++ * step
   }
}
