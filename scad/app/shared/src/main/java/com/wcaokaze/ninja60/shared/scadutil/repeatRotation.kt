package com.wcaokaze.ninja60.shared.scadutil

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

private data class RepeatRotation(
   override val parent: ScadParentObject,
   val count: Int
) : ScadParentObject() {
   override fun toScadRepresentation(): String {
      val increment = Angle.PI * 2 / count

      return buildChildrenScad(
         "for (a=[0:${increment.toScadRepresentation()}:360]) rotate([0, 0, a])")
   }
}

/**
 * Z軸の周りに[count]個[children]を出力する
 */
fun ScadParentObject.repeatRotation(
   count: Int,
   children: ScadParentObject.() -> Unit
): ScadObject {
   val repeatRotation = RepeatRotation(this, count)
   addChild(repeatRotation)
   repeatRotation.children()
   return repeatRotation
}
