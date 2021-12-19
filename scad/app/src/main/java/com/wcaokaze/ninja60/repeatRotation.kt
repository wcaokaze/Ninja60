package com.wcaokaze.ninja60

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * Z軸を軸として[child]を回転させて[count]回出力する
 */
fun ScadParentObject.repeatRotation(
   count: Int,
   child: ScadParentObject.() -> Unit
): ScadObject {
   val twoPi = Angle.PI * 2

   return union {
      for (a in 0.0.rad..twoPi step twoPi / count) {
         rotate(z = a) {
            child()
         }
      }
   }
}
