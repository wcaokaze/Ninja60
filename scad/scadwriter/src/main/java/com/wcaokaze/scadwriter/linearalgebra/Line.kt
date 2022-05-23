package com.wcaokaze.scadwriter.linearalgebra

import com.wcaokaze.scadwriter.foundation.*

class Line2d
   /** pointを通りdirectionと平行な直線 */
   constructor(private val point: Point2d, private val direction: Vector2d)
{
   companion object {
      val X_AXIS = Line2d(Point2d.ORIGIN, Vector2d.X_UNIT_VECTOR)
      val Y_AXIS = Line2d(Point2d.ORIGIN, Vector2d.Y_UNIT_VECTOR)
   }

   /** 2点を通る直線 */
   constructor(a: Point2d, b: Point2d) : this(a, Vector2d(a, b))

   /**
    * 指定したX座標でのこの直線上の点の座標を返します。
    *
    * この直線が指定したX座標を通らない場合(Y軸と並行など)、
    * 返り値の座標には[無限][Double.POSITIVE_INFINITY]を含むことがありますが、
    * nullを返したり例外をスローすることはありません。
    */
   fun pointAtX(x: Point) = Point2d(
      x,
      y = point.y + (x - point.x) * (direction.y / direction.x)
   )

   /**
    * 指定したY座標でのこの直線上の点の座標を返します。
    *
    * この直線が指定したY座標を通らない場合(X軸と並行など)、
    * 返り値の座標には[無限][Double.POSITIVE_INFINITY]を含むことがありますが、
    * nullを返したり例外をスローすることはありません。
    */
   fun pointAtY(y: Point) = Point2d(
      x = point.x + (y - point.y) * (direction.x / direction.y),
      y
   )

   /**
    * この直線の向きを表すベクトルを返す。長さは1mm
    */
   val vector: Vector2d get() = vector.toUnitVector()
}

class Line3d
   /** pointを通りdirectionと平行な直線 */
   constructor(private val point: Point3d, private val direction: Vector3d)
{
   companion object {
      val X_AXIS = Line3d(Point3d.ORIGIN, Vector3d.X_UNIT_VECTOR)
      val Y_AXIS = Line3d(Point3d.ORIGIN, Vector3d.Y_UNIT_VECTOR)
      val Z_AXIS = Line3d(Point3d.ORIGIN, Vector3d.Z_UNIT_VECTOR)
   }

   /** 2点を通る直線 */
   constructor(a: Point3d, b: Point3d) : this(a, Vector3d(a, b))

   /**
    * この直線上の一点を返す。
    * どこでもいいから直線上の点がほしいときに。
    */
   internal val somePoint: Point3d get() = point

   /**
    * 指定したX座標でのこの直線上の点の座標を返します。
    *
    * この直線が指定したX座標を通らない場合(Y軸と並行など)、
    * 返り値の座標には[無限][Double.POSITIVE_INFINITY]を含むことがありますが、
    * nullを返したり例外をスローすることはありません。
    */
   fun pointAtX(x: Point) = Point3d(
      x,
      y = point.y + (x - point.x) * (direction.y / direction.x),
      z = point.z + (x - point.x) * (direction.z / direction.x)
   )

   /**
    * 指定したY座標でのこの直線上の点の座標を返します。
    *
    * この直線が指定したY座標を通らない場合(Z軸と並行など)、
    * 返り値の座標には[無限][Double.POSITIVE_INFINITY]を含むことがありますが、
    * nullを返したり例外をスローすることはありません。
    */
   fun pointAtY(y: Point) = Point3d(
      x = point.x + (y - point.y) * (direction.x / direction.y),
      y,
      z = point.z + (y - point.y) * (direction.z / direction.y)
   )

   /**
    * 指定したZ座標でのこの直線上の点の座標を返します。
    *
    * この直線が指定したZ座標を通らない場合(X軸と並行など)、
    * 返り値の座標には[無限][Double.POSITIVE_INFINITY]を含むことがありますが、
    * nullを返したり例外をスローすることはありません。
    */
   fun pointAtZ(z: Point) = Point3d(
      x = point.x + (z - point.z) * (direction.x / direction.z),
      y = point.y + (z - point.z) * (direction.y / direction.z),
      z
   )

   /**
    * この直線の向きを表すベクトルを返す。長さは1mm
    */
   val vector: Vector3d get() = direction.toUnitVector()
}

fun Line3d.translate(distance: Size3d)
      = Line3d(somePoint.translate(distance), vector)

fun Line3d.translate(distance: Vector3d): Line3d
      = translate(Size3d(distance.x, distance.y, distance.z))

fun Line3d.translate(direction: Vector3d, distance: Size): Line3d
      = translate(direction.toUnitVector() * distance.numberAsMilliMeter)

fun Line3d.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm
): Line3d = translate(Size3d(x, y, z))

fun Line3d.rotate(axis: Line3d, angle: Angle) = Line3d(
   somePoint.rotate(axis, angle),
   vector.rotate(axis.vector, angle)
)
