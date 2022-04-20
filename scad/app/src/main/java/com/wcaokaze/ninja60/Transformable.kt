package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * 回転や移動が可能なモデル。
 */
interface Transformable<T : Transformable<T>> {
   /** このモデルの位置の基準となる点。[KeyPlate]の場合は中央の点といった感じ */
   val referencePoint: Point3d

   /** このモデルの手前方向を表すベクトル。 */
   val frontVector: Vector3d

   /** このモデルの下方向を表すベクトル。 */
   val bottomVector: Vector3d

   fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d): T

   fun translate(distance: Size3d): T
         = copy(referencePoint.translate(distance), frontVector, bottomVector)

   fun translate(distance: Vector3d): T
         = copy(referencePoint.translate(distance), frontVector, bottomVector)

   fun translate(direction: Vector3d, distance: Size): T
         = copy(referencePoint.translate(direction, distance), frontVector, bottomVector)

   fun translate(
      x: Size = 0.mm,
      y: Size = 0.mm,
      z: Size = 0.mm
   ): T = translate(Size3d(x, y, z))

   fun rotate(axis: Line3d, angle: Angle): T = copy(
      referencePoint.rotate(axis, angle),
      frontVector.rotate(axis.vector, angle),
      bottomVector.rotate(axis.vector, angle)
   )
}

val Transformable<*>.topVector:   Vector3d get() = -bottomVector
val Transformable<*>.backVector:  Vector3d get() = -frontVector
val Transformable<*>.rightVector: Vector3d get() = frontVector vectorProduct bottomVector
val Transformable<*>.leftVector:  Vector3d get() = -rightVector

val Transformable<*>.bottomVectorLine get() = Line3d(referencePoint, bottomVector)
val Transformable<*>.topVectorLine    get() = Line3d(referencePoint, topVector)
val Transformable<*>.frontVectorLine  get() = Line3d(referencePoint, frontVector)
val Transformable<*>.backVectorLine   get() = Line3d(referencePoint, backVector)
val Transformable<*>.rightVectorLine  get() = Line3d(referencePoint, rightVector)
val Transformable<*>.leftVectorLine   get() = Line3d(referencePoint, leftVector)

/**
 * [Y軸][Vector3d.Y_UNIT_VECTOR]の負の方向が[Transformable.frontVector]、
 * [Z軸][Vector3d.Z_UNIT_VECTOR]の負の方向が[Transformable.bottomVector]
 * を向くように[children]を回転し、
 * [原点][Point3d.ORIGIN]が[Transformable.referencePoint]になるように移動します
 */
fun ScadParentObject.place(
   transformable: Transformable<*>,
   children: ScadParentObject.() -> Unit
): ScadObject {
   var rotationReference = transformable
      .copy(Point3d.ORIGIN, -Vector3d.Y_UNIT_VECTOR, -Vector3d.Z_UNIT_VECTOR)

   var c: ScadObject = union(children)

   var axis = rotationReference.topVector vectorProduct transformable.topVector
   var angle = rotationReference.topVector angleWith transformable.topVector

   if (axis.norm > 0.mm) {
      c = c.rotate(angle, Point3d.ORIGIN.translate(axis))
      rotationReference = rotationReference
         .rotate(Line3d(rotationReference.referencePoint, axis), angle)
   }

   axis = rotationReference.frontVector vectorProduct transformable.frontVector
   angle = rotationReference.frontVector angleWith transformable.frontVector

   if (axis.norm > 0.mm) {
      c = c.rotate(angle, Point3d.ORIGIN.translate(axis))
      rotationReference = rotationReference
         .rotate(Line3d(rotationReference.referencePoint, axis), angle)
   }

   return c.translate(transformable.referencePoint - rotationReference.referencePoint)
}
