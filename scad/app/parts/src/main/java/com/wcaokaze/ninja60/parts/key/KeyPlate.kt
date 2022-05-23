package com.wcaokaze.ninja60.parts.key

import com.wcaokaze.scadwriter.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

/** 基板や各種プレートでキーひとつに当てられる領域 */
data class KeyPlate(
   override val referencePoint: Point3d,
   val size: Size2d,
   override val bottomVector: Vector3d,
   override val frontVector: Vector3d
) : TransformableDefaultImpl<KeyPlate> {
   init {
      val angle = bottomVector angleWith frontVector

      require(angle >= (90 - 0.01).deg && angle <= (90 + 0.01).deg) {
         "The angle formed by normalVector and frontVector must be 90 degrees"
      }
   }

   private fun point(x: Double, y: Double): Point3d {
      return referencePoint
         .translate(rightVector, size.x * x)
         .translate(frontVector, size.y * y)
   }

   val frontLeft:  Point3d get() = point(-0.5,  0.5)
   val frontRight: Point3d get() = point( 0.5,  0.5)
   val backRight:  Point3d get() = point( 0.5, -0.5)
   val backLeft:   Point3d get() = point(-0.5, -0.5)

   val points: List<Point3d>
      get() = listOf(frontLeft, frontRight, backRight, backLeft)

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = KeyPlate(referencePoint, size, bottomVector, frontVector)
}
