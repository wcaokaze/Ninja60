package com.wcaokaze.ninja60.shared.scadutil

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*
import kotlin.math.*

fun ScadParentObject.arcCylinder(
   radius: Size, height: Size,
   startAngle: Angle, endAngle: Angle,
   offset: Size = 0.mm
): ScadObject {
   val fixedRadius = radius * sqrt(2.0)

   val startOffset = Size2d(offset * cos(startAngle - 90.deg),
                            offset * sin(startAngle - 90.deg))
   val endOffset = Size2d(offset * cos(endAngle + 90.deg),
                          offset * sin(endAngle + 90.deg))

   fun arcPoint(a: Angle) = Point2d.ORIGIN + Size2d(fixedRadius * cos(a),
                                                    fixedRadius * sin(a))

   return intersection {
      cylinder(height, radius)

      linearExtrude(height) {
         polygon(
            listOf(
               Point2d.ORIGIN,
               Point2d.ORIGIN + startOffset,
               arcPoint(startAngle) + startOffset,
               *(startAngle..endAngle step Angle.PI / 4).map(::arcPoint).toTypedArray(),
               arcPoint(endAngle) + endOffset,
               Point2d.ORIGIN + endOffset
            )
         )
      }
   }
}

fun ScadParentObject.arcCylinder(
   innerRadius: Size, outerRadius: Size,
   height: Size,
   startAngle: Angle, endAngle: Angle,
   offset: Size = 0.mm
): ScadObject {
   return difference {
      arcCylinder(outerRadius, height, startAngle, endAngle, offset)

      translate(z = (-0.01).mm) {
         arcCylinder(
            innerRadius,
            height + 0.02.mm,
            startAngle - 0.1.deg,
            endAngle + 0.1.deg,
            offset
         )
      }
   }
}
