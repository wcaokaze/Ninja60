package com.wcaokaze.ninja60.shared.calcutil

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * *指定した平面における* 円弧の終点を返す。
 * @param origin 平面の原点とみなす点。[Point3d.ORIGIN]である必要はない
 * @param xAxis 平面のX軸とみなす向き。[Vector3d.X_UNIT_VECTOR]である必要はない
 * @param yAxis 平面のY軸とみなす向き。[Vector3d.Y_UNIT_VECTOR]である必要はない
 */
fun arcEndPoint(
   origin: Point3d, xAxis: Vector3d, yAxis: Vector3d,
   arcRadius: Size, angle: Angle, offset: Size
): Point3d {
   val zAxis = xAxis vectorProduct yAxis
   fun vector(angle: Angle) = xAxis.rotate(zAxis, angle)

   return origin
      .translate(vector(angle), arcRadius)
      .translate(vector(angle + 90.deg), offset)
}
