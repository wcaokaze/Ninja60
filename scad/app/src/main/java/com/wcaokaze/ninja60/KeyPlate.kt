package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.foundation.*

/** 基板や各種プレートでキーひとつに当てられる領域 */
data class KeyPlate(
   val center: Point3d,
   val size: Size2d,

   /**
    * 法線ベクトル。
    *
    * つまりこのプレートと垂直な方向のベクトル。
    */
   val normalVector: Vector3d,

   /**
    * このプレートの手前方向を向いたベクトル。
    *
    * つまり `center` から `frontVector` 方向へ `size.y / 2` 、
    * `normalVector` と `frontVector` の両方に垂直な方向へ `size.x / 2`
    * 移動した場所が `frontRight` となる。
    *
    * [法線ベクトル][normalVector]と垂直でなければならない。
    * 法線ベクトルと垂直でない場合はコンストラクタで例外がスローされる。
    */
   val frontVector: Vector3d
) {
   companion object {
      /** 1UのキーでのKeyPlateのサイズ */
      val SIZE = Size2d(16.mm, 16.mm)
   }

   init {
      val angle = normalVector angleWith frontVector

      require(angle >= (90 - 0.01).deg && angle <= (90 + 0.01).deg) {
         "The angle formed by normalVector and frontVector must be 90 degrees"
      }
   }

   private fun point(x: Double, y: Double): Point3d {
      val rightVector = normalVector vectorProduct frontVector

      return center
         .translate(rightVector, size.x * x)
         .translate(frontVector, size.y * y)
   }

   val frontLeft:  Point3d get() = point(-0.5,  0.5)
   val frontRight: Point3d get() = point( 0.5,  0.5)
   val backRight:  Point3d get() = point( 0.5, -0.5)
   val backLeft:   Point3d get() = point(-0.5, -0.5)

   val points: List<Point3d>
      get() = listOf(frontLeft, frontRight, backRight, backLeft)
}

fun KeyPlate.translate(distance: Size3d)
      = KeyPlate(center.translate(distance), size, normalVector, frontVector)

fun KeyPlate.translate(distance: Vector3d)
      = KeyPlate(center.translate(distance), size, normalVector, frontVector)

fun KeyPlate.translate(direction: Vector3d, distance: Size)
      = KeyPlate(center.translate(direction, distance), size, normalVector, frontVector)

fun KeyPlate.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm
): KeyPlate = translate(Size3d(x, y, z))

fun KeyPlate.rotate(axis: Line3d, angle: Angle) = KeyPlate(
   center.rotate(axis, angle),
   size,
   normalVector.rotate(axis.vector, angle),
   frontVector.rotate(axis.vector, angle)
)
