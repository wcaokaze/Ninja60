package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*
import com.wcaokaze.scadwriter.linearalgebra.*

data class Location(
   val point: Point3d,
   val frontVector: Vector3d,
   val bottomVector: Vector3d
) : Transformable<Location> {
   init {
      val angle = bottomVector angleWith frontVector

      require(angle in (90 - 0.01).deg .. (90 + 0.01).deg) {
         "The angle formed by normalVector and frontVector must be 90 degrees"
      }
   }

   val topVector: Vector3d get() = -bottomVector
   val backVector: Vector3d get() = -frontVector
   val rightVector: Vector3d get() = frontVector vectorProduct bottomVector
   val leftVector: Vector3d get() = -rightVector

   override fun translate(distance: Vector3d)
         = Location(point.translate(distance), frontVector, bottomVector)
   override fun translate(direction: Vector3d, distance: Size)
         = Location(point.translate(direction, distance), frontVector, bottomVector)

   override fun rotate(axis: Line3d, angle: Angle) = Location(
      point,
      frontVector.rotate(axis.vector, angle),
      bottomVector.rotate(axis.vector, angle)
   )
}

fun ScadParentObject.locate(
   point: Point3d = Point3d.ORIGIN,
   frontVector: Vector3d = -Vector3d.Y_UNIT_VECTOR,
   bottomVector: Vector3d = -Vector3d.Z_UNIT_VECTOR,
   children: ScadParentObject.() -> Unit
): ScadObject = locate(Location(point, frontVector, bottomVector), children)

fun ScadParentObject.locate(
   location: Location,
   children: ScadParentObject.() -> Unit
): ScadObject {
   var rotationReference = Location(
      Point3d.ORIGIN, -Vector3d.Y_UNIT_VECTOR, -Vector3d.Z_UNIT_VECTOR)

   var c: ScadObject = union(children)

   var axis = rotationReference.topVector vectorProduct location.topVector
   var angle = rotationReference.topVector angleWith location.topVector

   if (axis.norm > 0.mm) {
      c = c.rotate(angle, Point3d.ORIGIN.translate(axis))
      rotationReference = rotationReference
         .rotate(Line3d(rotationReference.point, axis), angle)
   }

   axis = rotationReference.frontVector vectorProduct location.frontVector
   angle = rotationReference.frontVector angleWith location.frontVector

   if (axis.norm > 0.mm) {
      c = c.rotate(angle, Point3d.ORIGIN.translate(axis))
      rotationReference = rotationReference
         .rotate(Line3d(rotationReference.point, axis), angle)
   }

   return c.translate(
      Vector3d(rotationReference.point, location.point))
}
