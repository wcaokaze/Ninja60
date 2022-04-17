package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*
import kotlin.math.*

val keyPitch = Size2d(19.2.mm, 16.mm)

fun ScadParentObject.polygonPyramid(n: Int, height: Size, radius: Size): ScadObject {
   return linearExtrude(height) {
      polygon(
         ((180.deg / n)..(360.deg / n) step (360.deg + 180.deg / n)).map {
            Point2d(
               Point(radius * sin(it)),
               Point(radius * cos(it))
            )
         }
      )
   }
}

fun ScadParentObject.arcCylinder(
   radius: Size, height: Size,
   startAngle: Angle, endAngle: Angle
): ScadObject {
   val fixedRadius = radius * sqrt(2.0)

   fun arcPoint(a: Angle) = Point2d.ORIGIN + Size2d(fixedRadius * cos(a),
                                                    fixedRadius * sin(a))

   return intersection {
      cylinder(height, radius)

      linearExtrude(height) {
         polygon(
            listOf(
               Point2d.ORIGIN,
               arcPoint(startAngle),
               *(startAngle..endAngle step Angle.PI / 4).map(::arcPoint).toTypedArray(),
               arcPoint(endAngle)
            )
         )
      }
   }
}

/**
 * [point]を通るこの平面の垂線のベクトル ([point]からこの平面を向く)
 */
private fun Plane3d.perpendicular(point: Point3d): Vector3d {
   val perpendicularLine = Line3d(point, normalVector)
   val intersectionPoint = this intersection perpendicularLine
   return Vector3d(point, intersectionPoint)
}

/**
 * 法線ベクトル方向を正としてこの平面と[point]の位置関係を比較します
 */
operator fun Plane3d.compareTo(point: Point3d): Int {
   val perpendicular = perpendicular(point)
   return when {
      perpendicular.norm < 0.001.mm -> 0
      perpendicular angleWith normalVector in (-90).deg..90.deg -> 1
      else -> -1
   }
}

/**
 * 法線ベクトルの向きを正として2つの平面の位置を比較します
 * 2つの平面の法線ベクトルは同じ向きである必要があります
 */
operator fun Plane3d.compareTo(another: Plane3d): Int {
   require(normalVector angleWith another.normalVector < 0.01.deg)

   val line = Line3d(Point3d.ORIGIN, normalVector)
   val vAB = Vector3d(this intersection line, another intersection line)

   return when {
      vAB.norm < 0.001.mm -> 0
      vAB angleWith normalVector in (-90).deg..90.deg -> -1
      else -> 1
   }
}

/**
 * 法線ベクトルの向きを正としたとき、より大きい位置にある平面を返します
 * 2つの平面の法線ベクトルは同じ向きである必要があります
 */
fun max(a: Plane3d, b: Plane3d): Plane3d {
   return if (a < b) {
      b
   } else {
      a
   }
}

/** 指定したベクトルの向きを正として2点を比較するComparator。 */
class PointOnVectorComparator(val vector: Vector3d) : Comparator<Point3d> {
   override fun compare(o1: Point3d, o2: Point3d): Int
         = Plane3d(o1, vector).compareTo(Plane3d(o2, vector))
}

/** 指定したベクトルの向きを正としたとき、より大きい位置にある点を返します */
fun max(vector: Vector3d, a: Point3d, b: Point3d): Point3d {
   return if (Plane3d(a, vector) < Plane3d(b, vector)) {
      b
   } else {
      a
   }
}

/**
 * [原点][Point.ORIGIN]に近くなるようにdで減算します
 */
infix fun Point.closeOrigin(d: Size): Point {
   return when {
      this.distanceFromOrigin < -d -> this + d
      this.distanceFromOrigin <  d -> Point.ORIGIN
      else                         -> this - d
   }
}

/**
 * [原点][Point.ORIGIN]から遠ざかるように加算します
 */
infix fun Point.leaveOrigin(d: Size): Point {
   return if (this < Point.ORIGIN) {
      this - d
   } else {
      this + d
   }
}

/**
 * a, bの2点を通る直線の高さzにおける座標
 */
fun zPointOnLine(a: Point3d, b: Point3d, z: Point) = Point3d(
   a.x + (z - a.z) * ((b.x - a.x) / (b.z - a.z)),
   a.y + (z - a.z) * ((b.y - a.y) / (b.z - a.z)),
   z
)

/**
 * キーを2層に並べるときに便利なやつ
 *
 * @param x
 * 東西方向の位置。U単位
 * @param y
 * 南北方向の位置。U単位
 * @param rotationX
 * X軸の回転角度。
 * @param rotationY
 * Y軸の回転角度。
 * @param rotationZ
 * Z軸の回転角度。
 * @param isUpperLayer
 * trueで上の層に配置。このとき自動的にX軸で180°回転して裏返されます
 */
fun ScadParentObject.layout(
   x: Double, y: Double,
   rotationX: Angle = 0.0.rad, rotationY: Angle = 0.0.rad, rotationZ: Angle = 0.0.rad,
   isUpperLayer: Boolean = false,
   children: ScadParentObject.() -> Unit
): Translate {
   val keyDistance = 16.5.mm
   val upperLayerZOffset = 28.5.mm

   return translate(
      keyDistance * (x + 0.5),
      keyDistance * (y + 0.5),
      if (isUpperLayer) { upperLayerZOffset } else { 0.mm }
   ) {
      rotate(
         rotationX + if (isUpperLayer) { Angle.PI } else { 0.0.rad },
         rotationY,
         rotationZ
      ) {
         children()
      }
   }
}
