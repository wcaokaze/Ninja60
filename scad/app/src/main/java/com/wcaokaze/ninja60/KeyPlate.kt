package com.wcaokaze.ninja60

import com.wcaokaze.scadwriter.foundation.*

/** 基板や各種プレートでキーひとつに当てられる領域 */
data class KeyPlate(
   val frontLeft:  Point3d,
   val frontRight: Point3d,
   val backRight:  Point3d,
   val backLeft:   Point3d
) {
   companion object {
      /** 1UのキーでのKeyPlateのサイズ */
      val SIZE = Size2d(19.05.mm, 19.05.mm)

      /** 原点に配置された1UのKeyPlate */
      val U = KeyPlate(center = Point3d.ORIGIN, SIZE)

      operator fun invoke(center: Point3d, size: Size2d) = KeyPlate(
         frontLeft  = center + Size3d(-size.x / 2, -size.y / 2, 0.mm),
         frontRight = center + Size3d( size.x / 2, -size.y / 2, 0.mm),
         backRight  = center + Size3d( size.x / 2,  size.y / 2, 0.mm),
         backLeft   = center + Size3d(-size.x / 2,  size.y / 2, 0.mm),
      )
   }

   val points: List<Point3d>
      get() = listOf(frontLeft, frontRight, backRight, backLeft)
}

fun KeyPlate.translate(distance: Size3d) = KeyPlate(
   frontLeft .translate(distance),
   frontRight.translate(distance),
   backRight .translate(distance),
   backLeft  .translate(distance)
)

fun KeyPlate.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm
): KeyPlate = translate(Size3d(x, y, z))

fun KeyPlate.rotateX(axis: Point3d, angle: Angle) = KeyPlate(
   frontLeft .rotateX(axis, angle),
   frontRight.rotateX(axis, angle),
   backRight .rotateX(axis, angle),
   backLeft  .rotateX(axis, angle)
)

fun KeyPlate.rotateY(axis: Point3d, angle: Angle) = KeyPlate(
   frontLeft .rotateY(axis, angle),
   frontRight.rotateY(axis, angle),
   backRight .rotateY(axis, angle),
   backLeft  .rotateY(axis, angle)
)

fun KeyPlate.rotateZ(axis: Point3d, angle: Angle) = KeyPlate(
   frontLeft .rotateZ(axis, angle),
   frontRight.rotateZ(axis, angle),
   backRight .rotateZ(axis, angle),
   backLeft  .rotateZ(axis, angle)
)
