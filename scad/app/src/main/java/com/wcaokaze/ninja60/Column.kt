package com.wcaokaze.ninja60

import com.wcaokaze.scadwriter.foundation.*

/** [KeyPlate] を縦に4枚並べた列 */
data class Column(
   val row1: KeyPlate,
   val row2: KeyPlate,
   val row3: KeyPlate,
   val row4: KeyPlate
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
         val row3 = KeyPlate(row3Center, KeyPlate.SIZE)
         val layeredRow3 = row3.translate(z = layerDistance)

         val row2Angle = atan(keyPitchV / 2, radius) * 2
         val layeredRow2 = row3
            .translate(z = layerDistance)
            .rotateX(referencePoint, row2Angle)

         val row1Axis = row3Center
            .translate(y = keyPitchV / 2)
            .rotateX(referencePoint, row2Angle)
         val layeredRow1 = row3
            .translate(y = keyPitchV, z = layerDistance)
            .rotateX(referencePoint, row2Angle)
            .rotateX(row1Axis, 90.deg - row2Angle)

         val row4Axis = row3Center
            .translate(y = -keyPitchV / 2)
         val layeredRow4 = row3
            .translate(y = -keyPitchV, z = layerDistance)
            .rotateX(row4Axis, (-83).deg)

         return Column(layeredRow1, layeredRow2, layeredRow3, layeredRow4)
      }
   }

   val keyPlates: List<KeyPlate>
      get() = listOf(row1, row2, row3, row4)
}

fun Column.translate(distance: Size3d) = Column(
   row1.translate(distance),
   row2.translate(distance),
   row3.translate(distance),
   row4.translate(distance)
)

fun Column.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm
): Column = translate(Size3d(x, y, z))

fun Column.rotateX(axis: Point3d, angle: Angle) = Column(
   row1.rotateX(axis, angle),
   row2.rotateX(axis, angle),
   row3.rotateX(axis, angle),
   row4.rotateX(axis, angle)
)

fun Column.rotateY(axis: Point3d, angle: Angle) = Column(
   row1.rotateY(axis, angle),
   row2.rotateY(axis, angle),
   row3.rotateY(axis, angle),
   row4.rotateY(axis, angle)
)

fun Column.rotateZ(axis: Point3d, angle: Angle) = Column(
   row1.rotateZ(axis, angle),
   row2.rotateZ(axis, angle),
   row3.rotateZ(axis, angle),
   row4.rotateZ(axis, angle)
)
