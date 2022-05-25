package com.wcaokaze.scadwriter.linearalgebra

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

fun ScadParentObject.mirror(plane: Plane3d, children: Mirror.() -> Unit): ScadParentObject {
   val offset = Vector3d(Point3d.ORIGIN, plane.somePoint)

   return translate(offset) {
      mirror(
         plane.normalVector.x,
         plane.normalVector.y,
         plane.normalVector.z
      ) {
         translate(-offset) {
            children()
         }
      }
   }
}
