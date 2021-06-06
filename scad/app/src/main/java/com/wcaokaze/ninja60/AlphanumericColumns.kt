package com.wcaokaze.ninja60

import com.wcaokaze.scadwriter.foundation.*

data class AlphanumericColumns(
   val column1: Column,
   val column2: Column,
   val column3: Column,
   val column4: Column,
   val column5: Column,
   val column6: Column
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

         return AlphanumericColumns(
            //    | dx           | dy      | dz  | radius | angle        |
            column(keyPitchH * -3, (-18).mm, 3.mm,   55.mm, (-2 * -2).deg),
            column(keyPitchH * -2, (-16).mm, 3.mm,   55.mm, (-2 * -2).deg),
            column(keyPitchH * -1, (- 5).mm, 2.mm,   57.mm, (-2 * -1).deg),
            column(keyPitchH *  0,    0 .mm, 0.mm,   58.mm, (-2 *  0).deg),
            column(keyPitchH *  1, (- 3).mm, 2.mm,   56.mm, (-2 *  1).deg),
            column(keyPitchH *  2, (- 5).mm, 2.mm,   56.mm, (-2 *  1).deg),
         )
      }
   }

   val columns: List<Column>
      get() = listOf(column1, column2, column3, column4, column5, column6)
}
