package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * [KeyPlate] をY軸方向に複数枚並べた列。
 *
 * Ninja60の実装では[keyPlates]は4枚のKeyPlateを生成する。
 *
 * 3行目を[referencePoint]の真下とし、
 * 2行目は[radius]を半径とする円弧上でちょうど3行目と接触する位置、
 * 1行目は2行目と接触する位置で3行目に対して(ほぼ)垂直、
 * 4行目は3行目と接触する位置で3行目に対して(ほぼ)垂直の位置に整列される。
 *
 * ただし4行目は若干深い位置に整列される。
 * これはDvorak配列において3行目の直後に同列の4行目を押す手順が多いためである。
 * つまり3行目を押した状態からそのまま4行目を押せるように
 * 4行目のキーは3行目のキーストローク分タテに長く、深い位置にある。
 *
 * @param bottomVector
 * このColumnの下向きのベクトル。全く回転していないColumnの場合Z軸の負の方向。
 * Ninja60では[referencePoint]から[bottomVector]方向に[radius]移動したところに
 * 3行目のKeyPlateが生成される。
 *
 * @param alignmentVector
 * KeyPlateを並べる方向をあらわすベクトル。
 * [referencePoint]と[bottomVector]でZ軸にあたる向きと位置が確定するが、
 * そのふたつだけでは確定しない、Y軸にあたる向きを決めるためのベクトル。
 *
 * @param layerDistance
 * 各[KeyPlate]が[referencePoint]側に移動する。
 * 0のときキーキャップの上面の位置となるので、
 * たとえば `-9.mm` でトッププレートの位置など
 *
 * @param twistAngle
 * [KeyPlate.frontVector]の向きの直線を軸として各KeyPlateを回転する。
 * ただし、軸とする直線の位置は回転結果が[KeyPlate.normalVector]側に上がるように選択される。
 * 具体的にはtwistAngleが正のときKeyPlateの左端、twistAngleが負のときKeyPlateの右端が
 * 軸となる。
 */
data class Column(
   val referencePoint: Point3d,
   val bottomVector: Vector3d,
   val alignmentVector: Vector3d,
   val radius: Size,
   val layerDistance: Size,
   val twistAngle: Angle
) {
   /** この列に含まれる[KeyPlate]のリスト。上から順 */
   val keyPlates: List<KeyPlate> get() {
      fun KeyPlate.twist(): KeyPlate {
         val axis = if (twistAngle > 0.deg) {
            Line3d(frontLeft, frontVector)
         } else {
            Line3d(frontRight, frontVector)
         }

         return rotate(axis, twistAngle)
      }

      val rightVector = alignmentVector vectorProduct bottomVector
      val alignmentAxis = Line3d(referencePoint, rightVector)

      val row3Center = referencePoint.translate(bottomVector, radius)
      val row3 = KeyPlate(
         row3Center, KeyPlate.SIZE,
         normalVector = -bottomVector,
         frontVector = alignmentVector
      )
      val layeredRow3 = row3
         .translate(bottomVector, -layerDistance)
         .twist()

      val row2Angle = atan(keyPitch.y / 2, radius) * 2
      val layeredRow2 = row3
         .translate(bottomVector, -layerDistance)
         .rotate(alignmentAxis, row2Angle)
         .twist()

      val row1Axis = Line3d(row3Center, rightVector)
         .translate(alignmentVector, -keyPitch.y / 2)
         .rotate(alignmentAxis, row2Angle)
      val layeredRow1 = row3
         .translate(bottomVector, -layerDistance)
         .translate(alignmentVector, -keyPitch.y)
         .rotate(alignmentAxis, row2Angle)
         .rotate(row1Axis, 90.deg - row2Angle)
         .twist()

      val row4Axis = Line3d(row3Center, rightVector)
         .translate(alignmentVector, keyPitch.y / 2)
      val layeredRow4 = row3
         .translate(bottomVector, -layerDistance)
         .translate(alignmentVector, keyPitch.y)
         .rotate(row4Axis, (-83).deg)
         .twist()

      return listOf(layeredRow1, layeredRow2, layeredRow3, layeredRow4)
   }
}

fun Column.translate(distance: Size3d) = Column(
   referencePoint.translate(distance),
   bottomVector,
   alignmentVector,
   radius,
   layerDistance,
   twistAngle
)

fun Column.translate(distance: Vector3d): Column
      = translate(Size3d(distance.x, distance.y, distance.z))

fun Column.translate(direction: Vector3d, distance: Size): Column
      = translate(direction.toUnitVector() * distance.numberAsMilliMeter)

fun Column.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm
): Column = translate(Size3d(x, y, z))

fun Column.rotate(axis: Line3d, angle: Angle) = Column(
   referencePoint.rotate(axis, angle),
   bottomVector.rotate(axis.vector, angle),
   alignmentVector.rotate(axis.vector, angle),
   radius,
   layerDistance,
   twistAngle
)
