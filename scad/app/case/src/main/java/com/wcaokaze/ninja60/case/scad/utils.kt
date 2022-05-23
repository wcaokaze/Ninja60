package com.wcaokaze.ninja60.case.scad

import com.wcaokaze.scadwriter.linearalgebra.*
import com.wcaokaze.ninja60.shared.scadutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

internal fun ScadParentObject.hugeCube(
   leftPlane:   Plane3d = Plane3d.YZ_PLANE.translate(x = (-200).mm),
   rightPlane:  Plane3d = Plane3d.YZ_PLANE.translate(x =   200 .mm),
   frontPlane:  Plane3d = Plane3d.ZX_PLANE.translate(y = (-200).mm),
   backPlane:   Plane3d = Plane3d.ZX_PLANE.translate(y =   200 .mm),
   bottomPlane: Plane3d = Plane3d.XY_PLANE.translate(z = (-200).mm),
   topPlane:    Plane3d = Plane3d.XY_PLANE.translate(z =   200 .mm)
): ScadObject {
   return distortedCube(
      topPlane, leftPlane, backPlane, rightPlane, frontPlane, bottomPlane)
}

internal fun ScadParentObject.distortedCube(
   topPlane: Plane3d,
   leftPlane: Plane3d,
   backPlane: Plane3d,
   rightPlane: Plane3d,
   frontPlane: Plane3d,
   bottomPlane: Plane3d
): ScadObject {
   return hullPoints(
      listOf(leftPlane, rightPlane).flatMap { x ->
         listOf(frontPlane, backPlane).flatMap { y ->
            listOf(bottomPlane, topPlane).map { z ->
               x intersection y intersection z
            }
         }
      }
   )
}
