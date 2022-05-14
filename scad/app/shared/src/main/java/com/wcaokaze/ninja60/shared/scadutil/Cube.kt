package com.wcaokaze.ninja60.shared.scadutil

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

data class Cube(
   override val referencePoint: Point3d,
   val size: Size3d,
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d
) : TransformableDefaultImpl<Cube> {
   companion object {
      operator fun invoke(
         size: Size3d,
         centerX: Boolean = false,
         centerY: Boolean = false,
         centerZ: Boolean = false
      ): Cube {
         var cube = Cube(
            Point3d.ORIGIN, size, -Vector3d.Y_UNIT_VECTOR, Vector3d.Z_UNIT_VECTOR
         )

         if (centerX) { cube = cube.translate(cube.leftVector,   cube.size.x / 2) }
         if (centerY) { cube = cube.translate(cube.frontVector,  cube.size.y / 2) }
         if (centerZ) { cube = cube.translate(cube.bottomVector, cube.size.z / 2) }

         return cube
      }

      operator fun invoke(size: Size3d, center: Boolean)
            = Cube(size, centerX = true, centerY = true, centerZ = true)
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = Cube(referencePoint, size, frontVector, bottomVector)
}

fun ScadParentObject.cube(cube: Cube): ScadObject {
   val points = listOf(0.mm, cube.size.x).flatMap { x ->
      listOf(0.mm, cube.size.y).flatMap { y ->
         listOf(0.mm, cube.size.z).map { z ->
            cube.referencePoint
               .translate(cube.rightVector, x)
               .translate(cube.backVector, y)
               .translate(cube.topVector, z)
         }
      }
   }

   return hullPoints(points)
}
