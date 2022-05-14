package com.wcaokaze.ninja60.shared.calcutil

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * 回転や移動が可能なモデル。
 */
interface Transformable<T : Transformable<T>> {
   fun translate(distance: Size3d): T
   fun translate(distance: Vector3d): T
   fun translate(direction: Vector3d, distance: Size): T

   fun translate(
      x: Size = 0.mm,
      y: Size = 0.mm,
      z: Size = 0.mm
   ): T = translate(Size3d(x, y, z))

   fun rotate(axis: Line3d, angle: Angle): T
}

interface Placeable<P : Placeable<P>> {
   /** このモデルの位置の基準となる点。[KeyPlate]の場合は中央の点といった感じ */
   val referencePoint: Point3d

   /** このモデルの手前方向を表すベクトル。 */
   val frontVector: Vector3d

   /** このモデルの下方向を表すベクトル。 */
   val bottomVector: Vector3d
}

interface TransformableDefaultImpl<P : TransformableDefaultImpl<P>>
   : Transformable<P>, Placeable<P>
{
   fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d): P

   override fun translate(distance: Size3d): P
         = copy(referencePoint.translate(distance), frontVector, bottomVector)

   override fun translate(distance: Vector3d): P
         = copy(referencePoint.translate(distance), frontVector, bottomVector)

   override fun translate(direction: Vector3d, distance: Size): P
         = copy(referencePoint.translate(direction, distance), frontVector, bottomVector)

   override fun rotate(axis: Line3d, angle: Angle): P = copy(
      referencePoint.rotate(axis, angle),
      frontVector.rotate(axis.vector, angle),
      bottomVector.rotate(axis.vector, angle)
   )
}

val Placeable<*>.topVector:   Vector3d get() = -bottomVector
val Placeable<*>.backVector:  Vector3d get() = -frontVector
val Placeable<*>.rightVector: Vector3d get() = frontVector vectorProduct bottomVector
val Placeable<*>.leftVector:  Vector3d get() = -rightVector

val Placeable<*>.bottomVectorLine get() = Line3d(referencePoint, bottomVector)
val Placeable<*>.topVectorLine    get() = Line3d(referencePoint, topVector)
val Placeable<*>.frontVectorLine  get() = Line3d(referencePoint, frontVector)
val Placeable<*>.backVectorLine   get() = Line3d(referencePoint, backVector)
val Placeable<*>.rightVectorLine  get() = Line3d(referencePoint, rightVector)
val Placeable<*>.leftVectorLine   get() = Line3d(referencePoint, leftVector)

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
