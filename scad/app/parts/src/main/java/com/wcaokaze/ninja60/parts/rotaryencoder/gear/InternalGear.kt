package com.wcaokaze.ninja60.parts.rotaryencoder.gear

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * 転位内歯車。[Gear]と噛み合います
 */
data class InternalGear(
   val module: Size,
   val toothCount: Int,
   val thickness: Size,
   override val referencePoint: Point3d,
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d
) : Transformable<InternalGear> {
   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = InternalGear(module, toothCount, thickness, referencePoint, frontVector, bottomVector)
}

infix fun InternalGear.idealDistance(gear: Gear): Size {
   require(module == gear.module) {
      "Attempt to get the distance even though their modules unmatching"
   }

   return module * (toothCount - gear.toothCount) / 2
}

private val InternalGear.hole
   get() = Gear(module, toothCount, thickness, referencePoint, frontVector, bottomVector)

val InternalGear.pitchDiameter: Size get() = hole.pitchDiameter
val InternalGear.pitchRadius: Size get() = hole.pitchRadius
val InternalGear.involuteDiameter: Size get() = hole.involuteDiameter
val InternalGear.involuteRadius: Size get() = hole.involuteRadius
val InternalGear.addendumDiameter: Size get() = hole.bottomDiameter
val InternalGear.addendumRadius: Size get() = hole.bottomRadius
val InternalGear.bottomDiameter: Size get() = hole.addendumDiameter
val InternalGear.bottomRadius: Size get() = hole.addendumRadius

/**
 * 内歯車の穴部分。
 *
 * 内歯車として[Gear]と噛み合わせるためには、他の物体からこれを[difference]して
 * 凹形の歯を作る必要がある
 */
fun ScadParentObject.internalGear(internalGear: InternalGear): ScadObject {
   return gear(internalGear.hole)
}
