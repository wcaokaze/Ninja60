package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * 親指で押すキーのうち、指の側面で押す方の扇形に並んだキースイッチを挿すためのプレート。
 * 指の腹で押す方のキーはこのプレートに含まれない
 *
 * [referencePoint]は親指のホームポジションのキーの位置、
 * [frontVector]は[referencePoint]から扇形の中心方向。
 */
data class ThumbPlate(
   override val referencePoint: Point3d,
   val layoutRadius: Size,
   val keyPitch: Size,
   override val frontVector: Vector3d,
   override val bottomVector: Vector3d
) : Transformable<ThumbPlate> {
   companion object {
      val KEY_PLATE_SIZE = Size2d(17.5.mm, 17.5.mm)
   }

   val keyAngle get() = Angle(keyPitch / layoutRadius)

   val keySwitches: List<KeySwitch> get() {
      val arcCenter = referencePoint.translate(frontVector, layoutRadius)
      val axis = Line3d(arcCenter, topVector)

      val referenceKeySwitch = KeySwitch(referencePoint, bottomVector, frontVector)

      return List(3) { referenceKeySwitch.rotate(axis, keyAngle * -it) }
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = ThumbPlate(referencePoint, layoutRadius, keyPitch, frontVector, bottomVector)
}
