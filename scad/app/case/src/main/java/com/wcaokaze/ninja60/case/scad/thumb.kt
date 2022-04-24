package com.wcaokaze.ninja60.case.scad

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.parts.key.*
import com.wcaokaze.ninja60.parts.key.thumb.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.ninja60.shared.scadutil.*
import com.wcaokaze.ninja60.shared.scadutil.Cube
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

internal fun ScadParentObject.thumbHomeKeyHole(
   key: KeySwitch,
   height: Size,
   bottomOffset: Size = 0.mm,
   frontOffset: Size = 0.mm,
   backOffset: Size = 0.mm,
   leftOffset: Size = 0.mm,
   rightOffset: Size = 0.mm
): ScadObject {
   val keyPlateSize = key.plate(ThumbPlate.KEY_PLATE_SIZE).size

   return cube(Cube(
      key.referencePoint
         .translate(key.leftVector,  keyPlateSize.x / 2 + leftOffset)
         .translate(key.frontVector, keyPlateSize.y / 2 + frontOffset)
         .translate(key.bottomVector, bottomOffset),
      Size3d(
         keyPlateSize.x + leftOffset + rightOffset,
         keyPlateSize.y + frontOffset + backOffset,
         height + bottomOffset),
      key.frontVector,
      key.bottomVector
   ))
}

internal fun ScadParentObject.thumbPlateCase(
   thumbPlate: ThumbPlate,
   height: Size,
   bottomOffset: Size = 0.mm,
   otherOffsets: Size = 0.mm
): ScadObject {
   val keyLength = thumbPlate.keySwitches
      .maxOf { ThumbPlate.KEY_PLATE_SIZE.y * it.layoutSize.y }
   val outerRadius = thumbPlate.layoutRadius + keyLength / 2
   val innerRadius = thumbPlate.layoutRadius - keyLength / 2

   val backAngle = Angle.PI / 2

   val startAngle = backAngle - thumbPlate.keyAngle * (thumbPlate.keySwitches.size - 0.5)
   val endAngle = backAngle + thumbPlate.keyAngle / 2

   return place(thumbPlate) {
      translate(y = -thumbPlate.layoutRadius, z = -bottomOffset) {
         difference {
            arcCylinder(
               innerRadius - otherOffsets,
               outerRadius + otherOffsets,
               height + bottomOffset,
               startAngle,
               endAngle,
               otherOffsets
            )
         }
      }
   }
}
