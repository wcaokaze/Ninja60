package com.wcaokaze.scadwriter.foundation

inline class Point(val distanceFromOrigin: Size) : Comparable<Point> {
   companion object {
      val ORIGIN = Point(0.mm)
   }

   override fun toString() = distanceFromOrigin.toString()

   operator fun times(n: Int)    = Point(distanceFromOrigin * n)
   operator fun times(n: Double) = Point(distanceFromOrigin * n)
   operator fun div  (n: Int)    = Point(distanceFromOrigin / n)
   operator fun div  (n: Double) = Point(distanceFromOrigin / n)

   operator fun plus (size: Size) = Point(distanceFromOrigin + size)
   operator fun minus(size: Size) = Point(distanceFromOrigin - size)
   operator fun minus(point: Point): Size = distanceFromOrigin - point.distanceFromOrigin

   override operator fun compareTo(other: Point): Int = distanceFromOrigin.compareTo(other.distanceFromOrigin)

   operator fun unaryMinus() = Point(-distanceFromOrigin)
   operator fun unaryPlus () = Point(+distanceFromOrigin)

   operator fun rangeTo(end: Point) = PointRange(this, end)
}

data class PointRange(override val start: Point,
                      override val endInclusive: Point) : ClosedRange<Point>
{
   infix fun step(step: Size) = Iterable {
      object : Iterator<Point> {
         private val precision = step / 2.0
         private var nextIndex = 0

         override fun hasNext() = if (step > 0.mm) {
            start + step * nextIndex <= endInclusive + precision
         } else {
            start + step * nextIndex >= endInclusive - precision
         }

         override fun next() = start + step * nextIndex++
      }
   }
}

data class Point2d(val x: Point, val y: Point) {
   companion object {
      /** 原点 */
      val ORIGIN = Point2d(Point.ORIGIN, Point.ORIGIN)
   }

   override fun toString() = "[$x, $y]"

   operator fun plus (size: Size2d) = Point2d(x + size.x, y + size.y)
   operator fun minus(size: Size2d) = Point2d(x - size.x, y - size.y)
   operator fun minus(point: Point2d) = Size2d(x - point.x, y - point.y)
}

data class Point3d(val x: Point, val y: Point, val z: Point) {
   companion object {
      /** 原点 */
      val ORIGIN = Point3d(Point.ORIGIN, Point.ORIGIN, Point.ORIGIN)
   }

   override fun toString() = "[$x, $y, $z]"

   operator fun plus (size: Size3d) = Point3d(x + size.x, y + size.y, z + size.z)
   operator fun minus(size: Size3d) = Point3d(x - size.x, y - size.y, z - size.z)
   operator fun minus(point: Point3d) = Size3d(x - point.x, y - point.y, z - point.z)
}
