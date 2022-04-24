package com.wcaokaze.ninja60.case

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.parts.rotaryencoder.gear.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.scadwriter.foundation.*

internal fun <T : Transformable<T>> T.transform(transformer: T.() -> T): T {
   return transformer()
}

/** 平面が指定した点を通るように移動します */
internal fun Plane3d.translate(point: Point3d) = Plane3d(point, normalVector)

/** 歯車に接するように移動します */
internal fun Plane3d.translateTangential(gear: Gear): Plane3d {
   // 円の接点から中心へ引いた直線は必ず接線と垂直になる性質を利用すれば瞬殺で出ます
   return Plane3d(
      gear.referencePoint.translate(normalVector, gear.addendumRadius),
      normalVector
   )
}

internal infix fun Vector3d.isSameDirection(another: Vector3d): Boolean
      = this angleWith another in (-90).deg..90.deg

internal class PointCombination(
   val pointA: Point3d,
   val pointB: Point3d,
   val otherPoints: List<Point3d>
) {
   val vectorAB get() = Vector3d(pointA, pointB)
}

internal fun Sequence<Point3d>.iterateAllCombination(): Sequence<PointCombination> {
   val allPoints = this
   return flatMap { a ->
      (allPoints - a).map { b ->
         PointCombination(
            a, b,
            otherPoints = (allPoints - a - b).toList()
         )
      }
   }
}
