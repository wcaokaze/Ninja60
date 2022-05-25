package com.wcaokaze.scadwriter.foundation

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.linearalgebra.*
import kotlin.math.*

data class Point(val distanceFromOrigin: Size)
   : ScadValue(), Comparable<Point>
{
   companion object {
      val ORIGIN = Point(0.mm)
   }

   override fun toString() = "Point($distanceFromOrigin)"
   override fun toScadRepresentation() = distanceFromOrigin.toScadRepresentation()

   operator fun plus (size: Size) = Point(distanceFromOrigin + size)
   operator fun minus(size: Size) = Point(distanceFromOrigin - size)
   operator fun minus(point: Point): Size = distanceFromOrigin - point.distanceFromOrigin

   override operator fun compareTo(other: Point): Int = distanceFromOrigin.compareTo(other.distanceFromOrigin)

   operator fun rangeTo(end: Point) = PointRange(this, end)
}

data class PointRange(override val start: Point,
                      override val endInclusive: Point) : ClosedRange<Point>
{
   infix fun step(step: Size) = Iterable {
      object : Iterator<Point> {
         private val precision = step / 16.0
         private var nextIndex = 0

         override fun hasNext() = if (start < endInclusive) {
            start + step * nextIndex <= endInclusive + precision
         } else {
            start - step * nextIndex >= endInclusive - precision
         }

         override fun next() = if (start < endInclusive) {
            start + step * nextIndex++
         } else {
            start - step * nextIndex++
         }
      }
   }
}

data class Point2d(val x: Point, val y: Point) : ScadValue() {
   companion object {
      /** 原点 */
      val ORIGIN = Point2d(Point.ORIGIN, Point.ORIGIN)
   }

   fun rotate(axis: Point2d, angle: Angle): Point2d {
      val px = axis.x
      val py = axis.y

      val tx = x - px
      val ty = y - py

      return Point2d(
         x = px + tx * cos(angle) - ty * sin(angle),
         y = py + tx * sin(angle) + ty * cos(angle)
      )
   }

   override fun toString() = "Point(${x.distanceFromOrigin}, ${y.distanceFromOrigin})"

   override fun toScadRepresentation()
         = "[${x.toScadRepresentation()}, ${y.toScadRepresentation()}]"

   operator fun plus (size: Size2d) = Point2d(x + size.x, y + size.y)
   operator fun minus(size: Size2d) = Point2d(x - size.x, y - size.y)
   operator fun minus(point: Point2d) = Size2d(x - point.x, y - point.y)

   operator fun rangeTo(end: Point2d) = Point2dRange(this, end)
}

data class Point2dRange(val start: Point2d,
                        val endInclusive: Point2d)
{
   infix fun step(step: Size) = Iterable {
      object : Iterator<Point2d> {
         private val precision = step / 16.0
         private var nextIndex = 0

         private val vector = endInclusive - start

         private val norm = Size(
            sqrt(vector.x.numberAsMilliMeter * vector.x.numberAsMilliMeter
               + vector.y.numberAsMilliMeter * vector.y.numberAsMilliMeter)
         )

         override fun hasNext() = step * nextIndex <= norm + precision

         override fun next(): Point2d {
            return start + Size2d(vector.x * (step / norm),
                                  vector.y * (step / norm)) * nextIndex++
         }
      }
   }
}

data class Point3d(val x: Point, val y: Point, val z: Point)
   : ScadValue(), Transformable<Point3d>
{
   companion object {
      /** 原点 */
      val ORIGIN = Point3d(Point.ORIGIN, Point.ORIGIN, Point.ORIGIN)
   }

   override fun translate(distance: Size3d) = Point3d(
      x + distance.x,
      y + distance.y,
      z + distance.z
   )

   override fun translate(distance: Vector3d): Point3d
         = translate(Size3d(distance.x, distance.y, distance.z))

   override fun translate(direction: Vector3d, distance: Size): Point3d
         = translate(direction.toUnitVector() * distance.numberAsMilliMeter)

   override fun rotate(axis: Line3d, angle: Angle): Point3d {
      val ax = axis.vector.x.numberAsMilliMeter
      val ay = axis.vector.y.numberAsMilliMeter
      val az = axis.vector.z.numberAsMilliMeter

      val (px, py, pz) = axis.somePoint

      val tx = x - px
      val ty = y - py
      val tz = z - pz

      return Point3d(
         x = px + tx * (cos(angle) + ax * ax * (1.0 - cos(angle)))
                + ty * (ax * ay * (1.0 - cos(angle)) - az * sin(angle))
                + tz * (az * ax * (1.0 - cos(angle)) + ay * sin(angle)),

         y = py + tx * (ax * ay * (1.0 - cos(angle)) + az * sin(angle))
                + ty * (cos(angle) + ay * ay * (1.0 - cos(angle)))
                + tz * (ay * az * (1.0 - cos(angle)) - ax * sin(angle)),

         z = pz + tx * (az * ax * (1.0 - cos(angle)) - ay * sin(angle))
                + ty * (ay * az * (1.0 - cos(angle)) + ax * sin(angle))
                + tz * (cos(angle) + az * az * (1.0 - cos(angle)))
      )
   }

   override fun toString()
         = "Point(${x.distanceFromOrigin}, ${y.distanceFromOrigin}, ${z.distanceFromOrigin})"

   override fun toScadRepresentation()
         = "[${x.toScadRepresentation()}, ${y.toScadRepresentation()}, ${z.toScadRepresentation()}]"

   operator fun plus (size: Size3d) = Point3d(x + size.x, y + size.y, z + size.z)
   operator fun minus(size: Size3d) = Point3d(x - size.x, y - size.y, z - size.z)
   operator fun minus(point: Point3d) = Size3d(x - point.x, y - point.y, z - point.z)

   operator fun rangeTo(end: Point3d) = Point3dRange(this, end)
}

data class Point3dRange(val start: Point3d,
                        val endInclusive: Point3d)
{
   infix fun step(step: Size) = Iterable {
      object : Iterator<Point3d> {
         private val precision = step / 16.0
         private var nextIndex = 0

         private val vector = endInclusive - start

         private val norm = Size(
            sqrt(vector.x.numberAsMilliMeter * vector.x.numberAsMilliMeter
               + vector.y.numberAsMilliMeter * vector.y.numberAsMilliMeter
               + vector.z.numberAsMilliMeter * vector.z.numberAsMilliMeter)
         )

         override fun hasNext() = step * nextIndex <= norm + precision

         override fun next(): Point3d {
            return start + Size3d(vector.x * (step / norm),
                                  vector.y * (step / norm),
                                  vector.z * (step / norm)) * nextIndex++
         }
      }
   }
}

fun Iterable<Point>  .average() = Point(map { it.distanceFromOrigin } .average())
fun Iterable<Point2d>.average() = Point2d(map { it.x } .average(), map { it.y } .average())
fun Iterable<Point3d>.average() = Point3d(map { it.x } .average(), map { it.y } .average(), map { it.z } .average())
