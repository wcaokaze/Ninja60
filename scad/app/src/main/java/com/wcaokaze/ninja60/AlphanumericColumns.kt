package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.foundation.*
import kotlin.math.abs

/** 人差し指から小指の4本の指で押す、アルファベットと数字のキーの行列 */
data class AlphanumericColumns(
   /** 小指側から人差し指側の順 */
   val columns: List<Column>
) {
   companion object {
      operator fun invoke(
         layerDistance: Size
      ): AlphanumericColumns {
         fun column(
            dx: Size, dy: Size, dz: Size,
            radius: Size,
            az: Angle, ax: Angle,
            twist: Angle
         ): Column {
            return Column(
                  Point3d.ORIGIN.translate(y = dy),
                  -Vector3d.Z_UNIT_VECTOR,
                  -Vector3d.Y_UNIT_VECTOR,
                  radius,
                  layerDistance,
                  twist
               )
               .rotate(
                  Line3d.Z_AXIS.translate(y = (-30).mm),
                  az
               )
               .let { column ->
                  column.rotate(
                     Line3d(
                        column.referencePoint
                           .translate(column.bottomVector.toUnitVector() * radius.numberAsMilliMeter),
                        column.alignmentVector
                     ),
                     ax
                  )
               }
               .translate(x = dx, z = dz)
         }

         return AlphanumericColumns(listOf(
            //    | dx                     | dy      | dz   | radius | az      | ax      | twist   |
            column(keyPitch.x * -2 - 18.mm, (-18).mm,  4.mm,   55.mm,   4 .deg, (-6).deg, (-8).deg),
            column(keyPitch.x * -2        , (-16).mm,  3.mm,   55.mm,   4 .deg, (-3).deg,   0 .deg),
            column(keyPitch.x * -1        , (- 5).mm,  2.mm,   57.mm,   2 .deg, (-1).deg,   0 .deg),
            column(keyPitch.x *  0        ,    0 .mm,  0.mm,   58.mm,   0 .deg,   1 .deg,   0 .deg),
            column(keyPitch.x *  1        , (- 3).mm,  2.mm,   56.mm, (-2).deg,   3 .deg,   0 .deg),
            column(keyPitch.x *  1 + 18.mm, (- 5).mm,  3.mm,   56.mm, (-2).deg,   6 .deg,   8 .deg),
         ))
      }
   }
}
