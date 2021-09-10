package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
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
