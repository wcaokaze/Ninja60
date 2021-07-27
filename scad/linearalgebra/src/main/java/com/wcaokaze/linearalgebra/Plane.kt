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
      val (px1, py1, pz1) = point
      val vx1 = normalVector.x.numberAsMilliMeter
      val vy1 = normalVector.y.numberAsMilliMeter
      val vz1 = normalVector.z.numberAsMilliMeter

      val (px2, py2, pz2) = line.somePoint
      val vx2 = line.vector.x.numberAsMilliMeter
      val vy2 = line.vector.y.numberAsMilliMeter
      val vz2 = line.vector.z.numberAsMilliMeter

      val t = ((px1 - px2) * vx1 + (py1 - py2) * vy1 + (pz1 - pz2) * vz1) /
            (vx1 * vx2 + vy1 * vy2 + vz1 * vz2)

      return Point3d(
         px2 + t * vx2,
         py2 + t * vy2,
         pz2 + t * vz2
      )
   }

   infix fun intersection(plane: Plane3d): Line3d {
      val px1 = point.x.distanceFromOrigin.numberAsMilliMeter
      val py1 = point.y.distanceFromOrigin.numberAsMilliMeter
      val pz1 = point.z.distanceFromOrigin.numberAsMilliMeter
      val vx1 = normalVector.x.numberAsMilliMeter
      val vy1 = normalVector.y.numberAsMilliMeter
      val vz1 = normalVector.z.numberAsMilliMeter

      val px2 = plane.point.x.distanceFromOrigin.numberAsMilliMeter
      val py2 = plane.point.y.distanceFromOrigin.numberAsMilliMeter
      val pz2 = plane.point.z.distanceFromOrigin.numberAsMilliMeter
      val vx2 = plane.normalVector.x.numberAsMilliMeter
      val vy2 = plane.normalVector.y.numberAsMilliMeter
      val vz2 = plane.normalVector.z.numberAsMilliMeter

      val vector = normalVector vectorProduct plane.normalVector

      fun Point3d(x: Double, y: Double, z: Double)
            = Point3d(Point(Size(x)), Point(Size(y)), Point(Size(z)))

      val point = when {
         // ベクトルのX成分が0でないとき直線は必ずX=0を通るので、
         // 2平面の式にX=0を代入して連立、その解をこの直線が通る点とすることができる
         vector.x < (-0.01).mm || vector.x > 0.01.mm -> Point3d(
            0.0,
            ((vx1 * vz2 * px1) - (vz1 * vx2 * px2) + (vy1 * vz2 * py1) - (vz1 * vy2 * py2) + (vz1 * vz2 * pz1) - (vz1 * vz2 * pz2)) /  (vy1 * vz2 - vz1 * vy2),
            ((vz1 * vy2 * pz1) - (vy1 * vz2 * pz2) + (vx1 * vy2 * px1) - (vy1 * vx2 * px2) + (vy1 * vy2 * py1) - (vy1 * vy2 * py2)) / -(vy1 * vz2 - vz1 * vy2)
         )

         // 同様にY成分が0でないとき
         vector.y < (-0.01).mm || vector.y > 0.01.mm -> Point3d(
            ((vx1 * vz2 * px1) - (vz1 * vx2 * px2) + (vy1 * vz2 * py1) - (vz1 * vy2 * py2) + (vz1 * vz2 * pz1) - (vz1 * vz2 * pz2)) / -(vz1 * vx2 - vx1 * vz2),
            0.0,
            ((vy1 * vx2 * py1) - (vx1 * vy2 * py2) + (vz1 * vx2 * pz1) - (vx1 * vz2 * pz2) + (vx1 * vx2 * px1) - (vx1 * vx2 * px2)) /  (vz1 * vx2 - vx1 * vz2)
         )

         // 同様にZ成分が0でないとき
         vector.z < (-0.01).mm || vector.z > 0.01.mm -> Point3d(
            ((vz1 * vy2 * pz1) - (vy1 * vz2 * pz2) + (vx1 * vy2 * px1) - (vy1 * vx2 * px2) + (vy1 * vy2 * py1) - (vy1 * vy2 * py2)) /  (vx1 * vy2 - vy1 * vx2),
            ((vy1 * vx2 * py1) - (vx1 * vy2 * py2) + (vz1 * vx2 * pz1) - (vz2 * vx1 * pz2) + (vx1 * vx2 * px1) - (vx1 * vx2 * px2)) / -(vx1 * vy2 - vy1 * vx2),
            0.0
         )

         // ベクトルの成分がすべて0。2つの平面は平行で交線は存在しない。
         else -> throw IllegalArgumentException("2 planes don't have an intersection.")
      }

      return Line3d(point, vector)
   }
}

fun Plane3d.translate(distance: Size3d)
      = Plane3d(somePoint.translate(distance), normalVector)

fun Plane3d.translate(distance: Vector3d): Plane3d
      = translate(Size3d(distance.x, distance.y, distance.z))

fun Plane3d.translate(direction: Vector3d, distance: Size): Plane3d
      = translate(direction.toUnitVector() * distance.numberAsMilliMeter)

fun Plane3d.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm
): Plane3d = translate(Size3d(x, y, z))

fun Plane3d.rotate(axis: Line3d, angle: Angle) = Plane3d(
   somePoint.rotate(axis, angle),
   normalVector.rotate(axis.vector, angle)
)
