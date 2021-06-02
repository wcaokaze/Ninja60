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
       */
      operator fun invoke(
         referencePoint: Point3d,
         radius: Size
      ): Column {
         val row3Center = referencePoint - Size3d(0.mm, 0.mm, radius)
         val row3 = KeyPlate(row3Center, KeyPlate.SIZE)

         val row2Angle = atan(keyPitchV / 2, radius) * 2
         val row2 = row3.rotateX(referencePoint, row2Angle)

         val row1Axis = row3Center
            .translate(y = keyPitchV / 2)
            .rotateX(referencePoint, row2Angle)
         val row1 = row3
            .translate(y = keyPitchV)
            .rotateX(referencePoint, row2Angle)
            .rotateX(row1Axis, 90.deg - row2Angle)

         val row4Axis = row3Center
            .translate(y = -keyPitchV / 2)
         val row4 = row3
            .translate(y = -keyPitchV)
            .rotateX(row4Axis, (-83).deg)

         return Column(row1, row2, row3, row4)
      }
   }

   val keyPlates: List<KeyPlate>
      get() = listOf(row1, row2, row3, row4)
}
