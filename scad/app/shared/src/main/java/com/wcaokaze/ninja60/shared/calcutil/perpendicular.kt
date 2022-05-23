package com.wcaokaze.ninja60.shared.calcutil

import com.wcaokaze.scadwriter.linearalgebra.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * [point]を通るこの平面の垂線のベクトル ([point]からこの平面を向く)
 */
fun Plane3d.perpendicular(point: Point3d): Vector3d {
   val perpendicularLine = Line3d(point, normalVector)
   val intersectionPoint = this intersection perpendicularLine
   return Vector3d(point, intersectionPoint)
}
