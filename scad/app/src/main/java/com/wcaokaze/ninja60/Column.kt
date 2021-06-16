package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.foundation.*

/** [KeyPlate] を縦に複数枚並べた列 */
data class Column(
   /** この列に含まれる[KeyPlate]のリスト。上から順 */
   val keyPlates: List<KeyPlate>
) {
   companion object {
      /**
       * 指定されたパラメータからなるColumnを生成する。
       *
       * 3列目を[referencePoint]の真下とし、
       * 2列目は[radius]を半径とする円弧上でちょうど3列目と接触する位置、
       * 1列目は2列目と接触する位置で3列目に対して(ほぼ)垂直、
       * 4列目は3列目と接触する位置で3列目に対して(ほぼ)垂直の位置に整列される。
       *
       * ただし4列目は若干深い位置に整列される。
       * これはDvorak配列において3列目の直後に同列の4列目を押す手順が多いためである。
       * つまり3列目を押した状態からそのまま4列目を押せるように
       * 4列目のキーは3列目のキーストローク分長く、深い位置にある。
       *
       * @param layerDistance
       * 各[KeyPlate]が[referencePoint]側に移動する。
       * 0のときキーキャップの上面の位置となるので、
       * たとえば `-9.mm` でトッププレートの位置など
       */
      operator fun invoke(
         referencePoint: Point3d,
         radius: Size,
         layerDistance: Size
      ): Column {
         val row3Center = referencePoint - Size3d(0.mm, 0.mm, radius)
         val row3 = KeyPlate(
            row3Center, KeyPlate.SIZE,
            normalVector = Vector3d.Z_UNIT_VECTOR,
            frontVector = -Vector3d.Y_UNIT_VECTOR
         )
         val layeredRow3 = row3.translate(z = layerDistance)

         val row2Angle = atan(keyPitchV / 2, radius) * 2
         val layeredRow2 = row3
            .translate(z = layerDistance)
            .rotate(Line3d(referencePoint, Vector3d.X_UNIT_VECTOR), row2Angle)

         val row1Axis = row3Center
            .translate(y = keyPitchV / 2)
            .rotate(Line3d(referencePoint, Vector3d.X_UNIT_VECTOR), row2Angle)
         val layeredRow1 = row3
            .translate(y = keyPitchV, z = layerDistance)
            .rotate(Line3d(referencePoint, Vector3d.X_UNIT_VECTOR), row2Angle)
            .rotate(Line3d(row1Axis, Vector3d.X_UNIT_VECTOR), 90.deg - row2Angle)

         val row4Axis = row3Center
            .translate(y = -keyPitchV / 2)
         val layeredRow4 = row3
            .translate(y = -keyPitchV, z = layerDistance)
            .rotate(Line3d(row4Axis, Vector3d.X_UNIT_VECTOR), (-83).deg)

         return Column(listOf(layeredRow1, layeredRow2, layeredRow3, layeredRow4))
      }
   }
}

fun Column.translate(distance: Size3d) = Column(
   keyPlates.map { it.translate(distance) }
)

fun Column.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm
): Column = translate(Size3d(x, y, z))

fun Column.rotate(axis: Line3d, angle: Angle) = Column(
   keyPlates.map { it.rotate(axis, angle) }
)
