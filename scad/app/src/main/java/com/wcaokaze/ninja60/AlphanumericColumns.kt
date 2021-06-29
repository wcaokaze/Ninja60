package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.foundation.*

/** 人差し指から小指の4本の指で押す、アルファベットと数字のキーの行列 */
data class AlphanumericColumns(
   /** 小指側から人差し指側の順 */
   val columns: List<Column>
) {
   companion object {
      operator fun invoke(
         layerDistance: Size
      ): AlphanumericColumns {
         fun column(dx: Size, dy: Size, dz: Size, radius: Size, az: Angle, twist: Angle): Column {
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
               .translate(x = dx, z = dz)
         }

         return AlphanumericColumns(listOf(
            //    | dx                    | dy      | dz   | radius | az           | ax       |
            column(keyPitchH * -2 - 17.mm, (-18).mm,  3.mm,   55.mm, (-2 * -2).deg, (-15).deg),
            column(keyPitchH * -2        , (-16).mm,  3.mm,   55.mm, (-2 * -2).deg,    0 .deg),
            column(keyPitchH * -1        , (- 5).mm,  2.mm,   57.mm, (-2 * -1).deg,    0 .deg),
            column(keyPitchH *  0        ,    0 .mm,  0.mm,   58.mm, (-2 *  0).deg,    0 .deg),
            column(keyPitchH *  1        , (- 3).mm,  2.mm,   56.mm, (-2 *  1).deg,    0 .deg),
            column(keyPitchH *  1 + 17.mm, (- 5).mm,  2.mm,   56.mm, (-2 *  1).deg,   15 .deg),
         ))
      }
   }
}
