package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

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

   return c.translate(
      Vector3d(rotationReference.referencePoint, placable.referencePoint))
}
