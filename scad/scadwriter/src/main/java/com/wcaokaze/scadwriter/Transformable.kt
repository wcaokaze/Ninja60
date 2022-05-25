package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.linearalgebra.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * 回転や移動が可能なモデル。
 */
interface Transformable<T : Transformable<T>> {
   fun translate(distance: Vector3d): T
   fun translate(direction: Vector3d, distance: Size): T

   fun translate(
      x: Size = 0.mm,
      y: Size = 0.mm,
      z: Size = 0.mm
   ): T = translate(Vector3d(x, y, z))

   fun rotate(axis: Line3d, angle: Angle): T
}

interface TransformableDefaultImpl<P : TransformableDefaultImpl<P>>
   : Transformable<P>, Placeable<P>
{
   fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d): P

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
