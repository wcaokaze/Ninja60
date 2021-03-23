package com.wcaokaze.ninja60.scadgenerator

import com.wcaokaze.ninja60.scadgenerator.scadwriter.*
import com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation.*

fun ScadWriter.prepareSharedScads() {
   val fs = "\$fs"
   writeRawScad("""
      $fs = 0.1;

      module polygon_pyramid(n, r, h) {
         linear_extrude(h) polygon([
            for (a = [180.0 / n : 360.0 / n : 360.0 + 180.0 / n])
               [r * sin(a), r * cos(a)]
         ]);
      }
   """)
}
fun ScadWriter.polygonPyramid(n: Int, height: Size, radius: Size) {
   writeRawScad("polygon_pyramid($n, $radius, $height);")
}

/**
 * [原点][Point.ORIGIN]に近くなるようにdで減算します
 */
infix fun Point.closeOrigin(d: Size): Point {
   return when {
      this.distanceFromOrigin < -d -> this + d
      this.distanceFromOrigin <  d -> Point.ORIGIN
      else                         -> this - d
   }
}

/**
 * [原点][Point.ORIGIN]から遠ざかるように加算します
 */
infix fun Point.leaveOrigin(d: Size): Point {
   return if (this < Point.ORIGIN) {
      this - d
   } else {
      this + d
   }
}

/**
 * a, bの2点を通る直線の高さzにおける座標
 */
fun zPointOnLine(a: Point3d, b: Point3d, z: Point) = Point3d(
   a.x + (z - a.z) * ((b.x - a.x).numberAsMilliMeter / (b.z - a.z).numberAsMilliMeter),
   a.y + (z - a.z) * ((b.y - a.y).numberAsMilliMeter / (b.z - a.z).numberAsMilliMeter),
   z
)

/**
 * キーを2層に並べるときに便利なやつ
 *
 * @param x
 * 東西方向の位置。U単位
 * @param y
 * 南北方向の位置。U単位
 * @param rotationX
 * X軸の回転角度。
 * @param rotationY
 * Y軸の回転角度。
 * @param rotationZ
 * Z軸の回転角度。
 * @param isUpperLayer
 * trueで上の層に配置。このとき自動的にX軸で180°回転して裏返されます
 */
fun ScadWriter.layout(
   x: Double, y: Double,
   rotationX: Angle = 0.0.rad, rotationY: Angle = 0.0.rad, rotationZ: Angle = 0.0.rad,
   isUpperLayer: Boolean = false,
   children: ScadWriter.() -> Unit
) {
   val keyDistance = 16.5.mm
   val upperLayerZOffset = 28.5.mm

   translate(
      keyDistance * (x + 0.5),
      keyDistance * (y + 0.5),
      if (isUpperLayer) { upperLayerZOffset } else { 0.mm }
   ) {
      rotate(
         rotationX + if (isUpperLayer) { Angle.PI } else { 0.0.rad },
         rotationY,
         rotationZ
      ) {
         children()
      }
   }
}
