package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*
import com.wcaokaze.scadwriter.linearalgebra.*

data class Mirror(
   override val parent: ScadParentObject,
   val plane: Plane3d
) : ScadParentObject() {
   override fun toScadRepresentation(): String {
      fun blockScad(statement: String, childScad: String)
            = "$statement {\n" + childScad.prependIndent("  ") + "\n}"

      val offset = Vector3d(plane.somePoint, Point3d.ORIGIN)
      val revertOffset = -offset

      return blockScad("translate(${revertOffset.scad})",
         blockScad("mirror(${plane.normalVector.scad})",
            blockScad("translate(${offset.scad})",
               children.joinToString("\n") { it.scad },
            )
         )
      )
   }
}

inline fun ScadParentObject.mirror(
   plane: Plane3d,
   children: Mirror.() -> Unit
): Mirror {
   val mirror = Mirror(this, plane)
   addChild(mirror)
   mirror.children()
   return mirror
}
