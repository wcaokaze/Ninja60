package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.foundation.*

/** 人差し指から小指の4本の指で押す、アルファベットと数字のキーの行列 */
data class AlphanumericColumns(
   /** 小指側から人差し指側の順 */
   val columns: List<Column>
) {
   companion object {
      operator fun invoke(
         layerDistance: Size
      ): AlphanumericColumns {
         fun column(
            dx: Size, dy: Size, dz: Size,
            radius: Size,
            az: Angle, ax: Angle,
            twist: Angle
         ): Column {
            return Column(
                  Point3d.ORIGIN.translate(y = dy),
                  -Vector3d.Z_UNIT_VECTOR,
                  -Vector3d.Y_UNIT_VECTOR,
                  radius,
                  layerDistance,
                  twist
               )
               .rotate(
                  Line3d.Z_AXIS.translate(y = (-30).mm),
                  az
               )
               .let { column ->
                  column.rotate(
                     Line3d(
                        column.referencePoint
                           .translate(column.bottomVector, radius),
                        column.alignmentVector
                     ),
                     ax
                  )
               }
               .translate(x = dx, z = dz)
         }

         return AlphanumericColumns(listOf(
            //    | dx                    | dy      | dz     | radius | az      | ax        | twist   |
            column(keyPitch.x * -2 - 18.mm, (-18).mm,  9.5.mm,   38.mm,   4 .deg,   1.5 .deg, (-2).deg),
            column(keyPitch.x * -2        , (-16).mm, 10.0.mm,   38.mm,   4 .deg,   3.0 .deg,   0 .deg),
            column(keyPitch.x * -1        , (- 5).mm,  5.0.mm,   42.mm,   2 .deg,   2.0 .deg,   0 .deg),
            column(keyPitch.x *  0        ,    0 .mm,  0.0.mm,   44.mm,   0 .deg,   0.0 .deg,   0 .deg),
            column(keyPitch.x *  1        , (- 3).mm,  4.0.mm,   41.mm, (-2).deg, (-1.0).deg,   0 .deg),
            column(keyPitch.x *  1 + 18.mm, (- 5).mm,  4.1.mm,   41.mm, (-2).deg,   0.5 .deg,   2 .deg),
         ))
      }
   }
}

fun AlphanumericColumns.translate(distance: Size3d) = AlphanumericColumns(
   columns.map { it.translate(distance) }
)

fun AlphanumericColumns.translate(distance: Vector3d) = AlphanumericColumns(
   columns.map { it.translate(distance) }
)

fun AlphanumericColumns.translate(direction: Vector3d, distance: Size) = AlphanumericColumns(
   columns.map { it.translate(direction, distance) }
)

fun AlphanumericColumns.translate(
   x: Size = 0.mm,
   y: Size = 0.mm,
   z: Size = 0.mm
): AlphanumericColumns = translate(Size3d(x, y, z))

fun AlphanumericColumns.rotate(axis: Line3d, angle: Angle) = AlphanumericColumns(
   columns.map { it.rotate(axis, angle) }
)
