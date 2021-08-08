package com.wcaokaze.ninja60

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

class KeySwitch {
   companion object {
      val TRAVEL = 4.mm

      /** 押し込んでいない状態でのステムの先からスイッチの上面までの距離 */
      val STEM_HEIGHT = 4.mm

      /** (ステムを除いた)キースイッチの高さ */
      val HEIGHT = 11.1.mm

      /** トッププレートの表面からスイッチの底面(足の先ではない)までの距離 */
      val BOTTOM_HEIGHT = 5.mm

      /** スイッチの上面(ステムの先ではない)からトッププレートまでの距離 */
      val TOP_HEIGHT = HEIGHT - BOTTOM_HEIGHT
   }
}

fun ScadWriter.switchHole(keyPlate: KeyPlate) {
   val bottomVector = -keyPlate.normalVector

   /* ボトムハウジングの突起部分にハメる穴。本来1.5mmのプレートを使うところ */
   val mountPlateHole = keyPlate.copy(size = Size2d(14.mm, 14.mm))

   /* スイッチが入る穴。 */
   val switchHole = keyPlate.copy(size = Size2d(16.mm, 16.mm))

   union {
      hullPoints(
         mountPlateHole.translate(bottomVector, -(0.5).mm).points +
         mountPlateHole.translate(bottomVector,   2.0 .mm).points
      )

      hullPoints(
         switchHole.translate(bottomVector,                  1.5.mm).points +
         switchHole.translate(bottomVector, KeySwitch.BOTTOM_HEIGHT).points
      )
   }
}
