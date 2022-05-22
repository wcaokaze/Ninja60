package com.wcaokaze.ninja60.shared.scadutil

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

data class Location(
   val point: Point3d,
   val frontVector: Vector3d,
   val bottomVector: Vector3d
) : Transformable<Location> {
   val topVector:   Vector3d get() = -bottomVector
   val backVector:  Vector3d get() = -frontVector
   val rightVector: Vector3d get() = frontVector vectorProduct bottomVector
   val leftVector:  Vector3d get() = -rightVector

   override fun translate(distance: Size3d)
         = Location(point.translate(distance), frontVector, bottomVector)
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

val Location.bottomVectorLine get() = Line3d(point, bottomVector)
val Location.topVectorLine    get() = Line3d(point, topVector)
val Location.frontVectorLine  get() = Line3d(point, frontVector)
val Location.backVectorLine   get() = Line3d(point, backVector)
val Location.rightVectorLine  get() = Line3d(point, rightVector)
val Location.leftVectorLine   get() = Line3d(point, leftVector)

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

   return c.translate(location.point - rotationReference.point)
}

interface Placeable<P : Placeable<P>> {
   /** このモデルの位置の基準となる点。[KeyPlate]の場合は中央の点といった感じ */
   val referencePoint: Point3d

   /** このモデルの手前方向を表すベクトル。 */
   val frontVector: Vector3d

   /** このモデルの下方向を表すベクトル。 */
   val bottomVector: Vector3d
}

/**
 * [Y軸][Vector3d.Y_UNIT_VECTOR]の負の方向が[TransformableDefaultImpl.frontVector]、
 * [Z軸][Vector3d.Z_UNIT_VECTOR]の負の方向が[TransformableDefaultImpl.bottomVector]
 * を向くように[children]を回転し、
 * [原点][Point3d.ORIGIN]が[TransformableDefaultImpl.referencePoint]になるように移動します
 */
fun ScadParentObject.place(
   placable: Placeable<*>,
   children: ScadParentObject.() -> Unit
): ScadObject {
   class RotationReference(
      override val referencePoint: Point3d,
      override val frontVector: Vector3d,
      override val bottomVector: Vector3d
   ) : TransformableDefaultImpl<RotationReference> {
      override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
            = RotationReference(referencePoint, frontVector, bottomVector)
   }

   var rotationReference = RotationReference(
      Point3d.ORIGIN, -Vector3d.Y_UNIT_VECTOR, -Vector3d.Z_UNIT_VECTOR)

   var c: ScadObject = union(children)

   var axis = rotationReference.topVector vectorProduct placable.topVector
   var angle = rotationReference.topVector angleWith placable.topVector

   if (axis.norm > 0.mm) {
      c = c.rotate(angle, Point3d.ORIGIN.translate(axis))
      rotationReference = rotationReference
         .rotate(Line3d(rotationReference.referencePoint, axis), angle)
   }

   axis = rotationReference.frontVector vectorProduct placable.frontVector
   angle = rotationReference.frontVector angleWith placable.frontVector

   if (axis.norm > 0.mm) {
      c = c.rotate(angle, Point3d.ORIGIN.translate(axis))
      rotationReference = rotationReference
         .rotate(Line3d(rotationReference.referencePoint, axis), angle)
   }

   return c.translate(placable.referencePoint - rotationReference.referencePoint)
}
