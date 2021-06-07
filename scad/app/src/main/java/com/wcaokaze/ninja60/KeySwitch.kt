package com.wcaokaze.ninja60

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
