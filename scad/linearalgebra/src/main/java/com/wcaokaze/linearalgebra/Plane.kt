package com.wcaokaze.linearalgebra

import com.wcaokaze.scadwriter.foundation.*

class Plane3d
   /** pointを通りnormalVectorを法線ベクトルとする平面 */
   constructor(private val point: Point3d, val normalVector: Vector3d)
{
   companion object {
      /** X軸とY軸の両方を含む平面 */
      val XY_PLANE = Plane3d(Point3d.ORIGIN, Vector3d.Z_UNIT_VECTOR)
      /** Y軸とZ軸の両方を含む平面 */
      val YZ_PLANE = Plane3d(Point3d.ORIGIN, Vector3d.X_UNIT_VECTOR)
      /** Z軸とX軸の両方を含む平面 */
      val ZX_PLANE = Plane3d(Point3d.ORIGIN, Vector3d.Y_UNIT_VECTOR)
   }

   /**
    * この平面上の一点を返す。
    * どこでもいいから平面上の点がほしいときに。
    */
   internal val somePoint: Point3d get() = point

   infix fun intersection(line: Line3d): Point3d {
      val (ppx, ppy, ppz) = point
      val pvx = normalVector.x.numberAsMilliMeter
      val pvy = normalVector.y.numberAsMilliMeter
      val pvz = normalVector.z.numberAsMilliMeter

      val (lpx, lpy, lpz) = line.somePoint
      val lvx = line.vector.x.numberAsMilliMeter
      val lvy = line.vector.y.numberAsMilliMeter
      val lvz = line.vector.z.numberAsMilliMeter

      val t = ((ppx - lpx) * pvx + (ppy - lpy) * pvy + (ppz - lpz) * pvz) /
            (pvx * lvx + pvy * lvy + pvz * lvz)

      return Point3d(
         lpx + t * lvx,
         lpy + t * lvy,
         lpz + t * lvz
      )
   }
}

fun Plane3d.translate(distance: Size3d)
      = Plane3d(somePoint.translate(distance), normalVector)

fun Plane3d.translate(distance: Vector3d): Plane3d
      = translate(Size3d(distance.x, distance.y, distance.z))

fun Plane3d.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm
): Plane3d = translate(Size3d(x, y, z))

fun Plane3d.rotate(axis: Line3d, angle: Angle) = Plane3d(
   somePoint.rotate(axis, angle),
   normalVector.rotate(axis.vector, angle)
)
