package com.wcaokaze.ninja60

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
         fun column(dx: Size, dy: Size, dz: Size, radius: Size, angle: Angle): Column {
            return Column(
                  Point3d.ORIGIN.translate(y = dy),
                  radius,
                  layerDistance
               )
               .rotateZ(
                  Point3d.ORIGIN.translate(y = (-30).mm),
                  angle
               )
               .translate(x = dx, z = dz)
         }

         return AlphanumericColumns(listOf(
            //    | dx            | dy      | dz  | radius | angle        |
            column(keyPitchH * -3, (-18).mm, 3.mm,   55.mm, (-2 * -2).deg),
            column(keyPitchH * -2, (-16).mm, 3.mm,   55.mm, (-2 * -2).deg),
            column(keyPitchH * -1, (- 5).mm, 2.mm,   57.mm, (-2 * -1).deg),
            column(keyPitchH *  0,    0 .mm, 0.mm,   58.mm, (-2 *  0).deg),
            column(keyPitchH *  1, (- 3).mm, 2.mm,   56.mm, (-2 *  1).deg),
            column(keyPitchH *  2, (- 5).mm, 2.mm,   56.mm, (-2 *  1).deg),
         ))
      }
   }
}
