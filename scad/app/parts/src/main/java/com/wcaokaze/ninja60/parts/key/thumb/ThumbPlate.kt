package com.wcaokaze.ninja60.parts.key.thumb

import com.wcaokaze.scadwriter.linearalgebra.*
import com.wcaokaze.ninja60.parts.key.*
import com.wcaokaze.scadwriter.*
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
) : TransformableDefaultImpl<ThumbPlate> {
   companion object {
      val KEY_PLATE_SIZE = Size2d(17.5.mm, 24.mm)
   }

   private val arcInnerRadius get() = layoutRadius - KEY_PLATE_SIZE.y / 2
   val keyAngle get() = Angle(keyPitch / arcInnerRadius)

   val keySwitches: List<KeySwitch> get() {
      val arcCenter = referencePoint.translate(frontVector, layoutRadius)
      val axis = Line3d(arcCenter, topVector)

      val referenceKeySwitch = KeySwitch(referencePoint, bottomVector, frontVector)

      return List(3) { referenceKeySwitch.rotate(axis, keyAngle * -it) }
   }

   override fun copy(referencePoint: Point3d, frontVector: Vector3d, bottomVector: Vector3d)
         = ThumbPlate(referencePoint, layoutRadius, keyPitch, frontVector, bottomVector)
}
