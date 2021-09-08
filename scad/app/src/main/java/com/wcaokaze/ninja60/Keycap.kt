package com.wcaokaze.ninja60

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

val enableOnlyOuter = true
val enableLegends = false

val keycapInvisibleFa = 8.0

val keycapMargin = 0.375.mm
val keycapHeight = 6.93.mm
val keycapWallFa = 15.0 // 2.0

val dishR = 15.mm
val dishFa = 15.0 // 2.0

val tiltXr = 260.mm
val tiltYr = 130.mm

val thumbTiltR = 16.mm
val thumbTiltA = 3.deg

class Keycap {
   companion object {
      val THICKNESS = 1.5.mm
   }
}

fun arcLengthToAngle(arcR: Size, length: Size) = Angle(length.numberAsMilliMeter / arcR.numberAsMilliMeter)

/**
 * 親指用キーキャップ。
 * 底が扇型で皿部分は通常のキーキャップと同様長方形にシリンドリカルのカーブ。
 *
 * @param arcR
 * 底の扇型の半径
 * @param arcStartA
 * 底の扇型の開始角度
 * @param arcEndA
 * 底の扇型の終了角度
 * @param h
 * キーキャップの長さ。
 * 「半径arcRのarcStartAからarcEndAまでの扇型」から
 * 「半径arcR - hのarcStartAからarcEndAまでの扇型」を
 * くりぬいたバウムクーヘン型がキーキャップの底面となる。
 * @param dishOffset
 * 皿の位置。底面の扇型で言うところの0°の位置から
 * 扇型の接線の方向(Y軸と並行の方向)に動く
 * 皿は円筒形なので位置をずらすと結果的に皿が傾いているかのように見える
 * @param polishingMargin
 * ステムの十字部が太くなります。
 * 磨きなどする場合に削れる分を想定して指定しましょう
 */
fun ScadParentObject.thumbKeycap(
   arcR: Size, arcStartA: Angle, arcEndA: Angle,
   dishOffset: Size, h: Size, polishingMargin: Size = 0.mm
): ScadObject {
   val topW = 18.mm - (keyPitch.x - 11.mm)
   val topH = h     - (keyPitch.y - 11.mm)

   val bottomInnerR  = arcR - h     + 0.375.mm
   val bottomCenterR = arcR - h / 2
   val bottomOuterR  = arcR         - 0.375.mm

   val bottomArcStartA = arcStartA + arcLengthToAngle(bottomInnerR, 0.375.mm)
   val bottomArcEndA   = arcEndA   - arcLengthToAngle(bottomInnerR, 0.375.mm)

   val dishPositionZ = Point(dishOffset * sin(-acos(dishOffset, dishR)))
   val bottomZ = Point(0.mm)
   val topZ = dishPositionZ + keycapHeight

   fun ScadParentObject.dish(fa: Double): ScadObject {
      return translate(bottomCenterR, dishOffset, (topZ + dishR - 2.mm).distanceFromOrigin) {
         rotate(90.deg - thumbTiltA, 0.deg, 90.deg) {
            cylinder(height = 32.mm, dishR, center = true, fa)
         }
      }
   }

   fun ScadParentObject.arc(
      outerR: Size, innerR: Size,
      startA: Angle, endA: Angle,
      topW: Size, topH: Size,
      z: Point
   ): ScadObject {
      fun bottomInnerX(a: Angle) = Point(innerR * cos(a))
      fun bottomInnerY(a: Angle) = Point(innerR * sin(a))
      fun bottomOuterX(a: Angle) = Point(outerR * cos(a))
      fun bottomOuterY(a: Angle) = Point(outerR * sin(a))

      val topInnerX = Point(bottomCenterR + -topH / 2)
      val topOuterX = Point(bottomCenterR +  topH / 2)
      fun topY(a: Angle) = Point(-topW / 2) + topW * ((a - startA).numberAsRadian / (endA - startA).numberAsRadian)

      val points = listOf(
         listOf(startA, endA)
            .map { angle ->
               Point2d(
                  bottomInnerX(angle) + (topInnerX   - bottomInnerX(angle)) * (z.distanceFromOrigin.numberAsMilliMeter / topZ.distanceFromOrigin.numberAsMilliMeter),
                  bottomInnerY(angle) + (topY(angle) - bottomInnerY(angle)) * (z.distanceFromOrigin.numberAsMilliMeter / topZ.distanceFromOrigin.numberAsMilliMeter)
               )
            },
         (endA..startA step (startA - endA) / 16)
            .map { angle ->
               Point2d(
                  bottomOuterX(angle) + (topOuterX   - bottomOuterX(angle)) * (z.distanceFromOrigin.numberAsMilliMeter / topZ.distanceFromOrigin.numberAsMilliMeter),
                  bottomOuterY(angle) + (topY(angle) - bottomOuterY(angle)) * (z.distanceFromOrigin.numberAsMilliMeter / topZ.distanceFromOrigin.numberAsMilliMeter)
               )
            }
      )

      return translate(z = z.distanceFromOrigin) {
         linearExtrude(0.01.mm) {
            polygon(points.flatten())
         }
      }
   }

   fun ScadParentObject.roundArc(
      outerR: Size, innerR: Size,
      startA: Angle, endA: Angle,
      topW: Size, topH: Size,
      roundR: Size,
      z: Point,
      fa: Double
   ): ScadObject {
      return minkowski {
         arc(
            outerR - roundR,
            innerR + roundR,
            startA + arcLengthToAngle(innerR, roundR),
            endA   - arcLengthToAngle(innerR, roundR),
            topW - roundR * 2,
            topH - roundR * 2,
            z
         )

         cylinder(height = 0.01.mm, roundR, fa)
      }
   }

   fun ScadParentObject.outer(): ScadObject {
      val bottomR = 0.mm
      val topR = 3.mm

      fun rAt(z: Point): Size {
         val rate = (z - bottomZ).numberAsMilliMeter / (topZ - bottomZ).numberAsMilliMeter
         return bottomR + (topR - bottomR) * rate
      }

      return difference {
         union {
            val step = 0.5.mm
            for (z in bottomZ..topZ step step) {
               hull {
                  roundArc(
                     bottomOuterR,
                     bottomInnerR,
                     bottomArcStartA,
                     bottomArcEndA,
                     topW, topH,
                     rAt(z),
                     z,
                     keycapWallFa
                  )

                  roundArc(
                     bottomOuterR,
                     bottomInnerR,
                     bottomArcStartA,
                     bottomArcEndA,
                     topW, topH,
                     rAt(z + step),
                     z + step,
                     keycapWallFa
                  )
               }
            }
         }

         hull {
            cylinder(height = 0.01.mm, radius = bottomInnerR, fa = keycapWallFa)

            translate(z = topZ.distanceFromOrigin) {
               cube(bottomCenterR * 2 - topH, topW, 0.01.mm, center = true)
            }
         }

         dish(dishFa)
      }
   }

   fun ScadParentObject.inner(): ScadObject {
      return difference {
         hull {
            arc(
               bottomOuterR - Keycap.THICKNESS,
               bottomInnerR + Keycap.THICKNESS,
               bottomArcStartA + arcLengthToAngle(bottomInnerR, Keycap.THICKNESS),
               bottomArcEndA   - arcLengthToAngle(bottomInnerR, Keycap.THICKNESS),
               topW, topH,
               z = Point(0.mm)
            )

            translate(bottomCenterR, 0.mm, topZ.distanceFromOrigin - Keycap.THICKNESS) {
               cube(
                  topH - Keycap.THICKNESS * 2,
                  topW - Keycap.THICKNESS * 2,
                  0.01.mm,
                  center = true
               )
            }
         }

         hull {
            cylinder(
               height = 0.01.mm,
               radius = bottomInnerR + Keycap.THICKNESS,
               keycapInvisibleFa
            )

            translate(z = topZ.distanceFromOrigin - Keycap.THICKNESS) {
               cube(
                  bottomCenterR * 2 - (topH - Keycap.THICKNESS * 2),
                  topW - Keycap.THICKNESS * 2,
                  0.01.mm,
                  center = true
               )
            }
         }

         translate(z = -Keycap.THICKNESS) {
            dish(keycapInvisibleFa)
         }
      }
   }

   fun ScadParentObject.pillar(): ScadObject {
      return intersection {
         union {
            translate(z = 2.5.mm) {
               difference {
                  cylinder(height = 24.mm, radius = bottomCenterR + 1.mm, keycapInvisibleFa)
                  cylinder(height = 24.mm, radius = bottomCenterR - 1.mm, keycapInvisibleFa)
               }
            }

            translate(0.mm, (-1).mm, 2.5.mm) {
               cube(bottomOuterR, 2.mm, 24.mm)
            }

            translate(x = (bottomOuterR + bottomInnerR) / 2) {
               translate(z = 2.5.mm) {
                  polygonPyramid(16, height = 24.mm, radius = 4.3.mm)
               }

               val northSouthThickness = 1.05.mm + polishingMargin
               val eastWestThickness   = 1.25.mm + polishingMargin

               translate(z = 12.5.mm) { cube(northSouthThickness, 4.mm, 24.mm, center = true) }
               translate(z = 12.5.mm) { cube(4.mm, eastWestThickness,   24.mm, center = true) }
            }
         }

         outer()
      }
   }

   return translate(x = -arcR) {
      union {
         if (enableOnlyOuter) {
            outer()
         } else {
            difference {
               outer()
               inner()
            }

            pillar()
         }
      }
   }
}

/**
 * キーキャップ。子を渡すとintersectionによって外形が調整されます
 *
 * @param x
 * 東西方向のキーの位置。U(1Uのキーの長さを1とする)単位。
 * @param y
 * 南北方向のキーの位置。U単位。
 * @param w
 * 東西方向のキーの長さ。U単位。
 * @param h
 * 南北方向のキーの長さ。U単位。
 * @param legend
 * 刻印の文字列。
 * @param isFluentToNorth
 * 上面の凹みが北側のキーと繋がるようになります。
 * isCylindricalがtrueの場合は無視されます。
 * @param isFluentToSouth
 * 上面の凹みが南側のキーと繋がるようになります。
 * isCylindricalがtrueの場合は無視されます。
 * @param isCylindrical
 * 上面の凹みの形状。trueで円筒形、falseで球形。
 * @param isHomePosition
 * trueにするとホームポジションを指で確かめるための突起がつきます。
 * @param bottomZ
 * 外形の底面のZ座標。
 * この高さにおける幅がキーピッチいっぱいに広がるため、
 * 調整用の子に合わせてこの値を指定することで、
 * なるべくキー間の隙間を詰める効果を期待できます。
 * @param leftWallPadding
 * 外形の左側がさらに左に移動します。
 * @param rightWallPadding
 * 外形の右側がさらに右に移動します。
 * @param leftWallAngle
 * 外形の左側に角度がつきます。0°が北、90°が向き
 * @param rightWallAngle
 * 外形の右側に角度がつきます。0°が北、-90°が東向き
 * @param wallY
 * leftWallAngle, rightWallAngleを指定する場合の
 * このキーの中心のY座標。
 * この値が大きいほどキーの幅が広くなることになりますね
 * @param polishingMargin
 * ステムの十字部が太くなります。
 * 磨きなどする場合に削れる分を想定して指定しましょう
 */
fun ScadParentObject.keycap(
   x: Double, y: Double, w: Double = 1.0, h: Double = 1.0, legend: String,
   isFluentToNorth: Boolean = false, isFluentToSouth: Boolean = false,
   isCylindrical: Boolean = false, isHomePosition: Boolean = false,
   isThinPillar: Boolean = false, bottomZ: Point = Point(0.mm),
   leftWallPadding: Size = 0.mm, rightWallPadding: Size = 0.mm,
   leftWallAngle: Angle = 0.0.rad, rightWallAngle: Angle = 0.0.rad,
   wallY: Point = Point(0.mm),
   polishingMargin: Size = 0.mm,
   children: ScadParentObject.() -> Unit
): ScadObject {
   val topW = 13.mm + keyPitch.x * (w - 1)
   val topH = 13.mm + keyPitch.y * (h - 1)
   val bottomW = keyPitch.x * w - keycapMargin * 2
   val bottomH = keyPitch.y * h - keycapMargin * 2

   val bottomNorthY = Point( bottomH / 2)
   val bottomSouthY = Point(-bottomH / 2)
   val bottomNorthLeftX  = Point(-bottomW / 2 - leftWallPadding  + (wallY.distanceFromOrigin + bottomNorthY.distanceFromOrigin) / tan(90.deg + leftWallAngle))
   val bottomNorthRightX = Point( bottomW / 2 + rightWallPadding + (wallY.distanceFromOrigin + bottomNorthY.distanceFromOrigin) / tan(90.deg + rightWallAngle))
   val bottomSouthLeftX  = Point(-bottomW / 2 - leftWallPadding  + (wallY.distanceFromOrigin + bottomSouthY.distanceFromOrigin) / tan(90.deg + leftWallAngle))
   val bottomSouthRightX = Point( bottomW / 2 + rightWallPadding + (wallY.distanceFromOrigin + bottomSouthY.distanceFromOrigin) / tan(90.deg + rightWallAngle))

   // このキーキャップの原点から見た、本来の原点の座標までの距離。
   val keyboardOrigin = Size2d(keyPitch.x * -x, keyPitch.y * -y)

   val tiltXa = asin(-keyboardOrigin.x, tiltXr)
   val tiltYa = asin(-keyboardOrigin.y, tiltYr)

   /*
    * 高さ tiltXr の点を中心としてX方向に(Y軸方向の直線を軸として) tiltXa 回転
    * 高さ tiltYr の点を中心としてY方向に(X軸方向の直線を軸として) tiltYa 回転
    * X軸方向に -cos(tiltXr) 、Y軸方向に -cos(tiltYr) 移動する。
    *
    * 要するにtiltXa, tiltYaで回転し、XY座標は回転前の位置に戻してZ座標だけそのままにする。
    */
   fun ScadParentObject.rotateForTilt(children: ScadParentObject.() -> Unit): ScadObject {
      return translate(z = tiltXr * (1 - cos(-tiltXa)) + tiltYr * (1 - cos(tiltYa))) {
         rotate(tiltYa, -tiltXa) {
            children()
         }
      }
   }

   fun rotatePointForTilt(point: Point3d): Point3d {
      return Point3d(
         x = Point(
              point.x.distanceFromOrigin * cos(-tiltXa)
            + point.z.distanceFromOrigin * sin(-tiltXa)
         ),
         y = Point(
              point.y.distanceFromOrigin * cos(tiltYa)
            + point.x.distanceFromOrigin * sin(tiltYa) * sin(-tiltXa)
            - point.z.distanceFromOrigin * sin(tiltYa) * cos(-tiltXa)
         ),
         z = Point(
              point.y.distanceFromOrigin * sin(tiltYa)
            - point.x.distanceFromOrigin * cos(tiltYa) * sin(-tiltXa)
            + point.z.distanceFromOrigin * cos(tiltYa) * cos(-tiltXa)
         )
      ) +
      Size3d(0.mm, 0.mm, tiltXr * (1 - cos(-tiltXa)) + tiltYr * (1 - cos(tiltYa)))
   }

   fun ScadParentObject.dish(keycapHeight: Size, fa: Double): ScadObject {
      return if (isCylindrical) {
         minkowski {
            cube(
               keyPitch.x * (w - 1) + 0.001.mm,
               keyPitch.y * (h - 1) + 0.001.mm,
               0.001.mm,
               center = true
            )

            translate(z = keycapHeight) {
               rotateForTilt {
                  translate(z = dishR) {
                     rotate(-tiltYa) {
                        cylinder(height = 32.mm, radius = dishR, center = true, fa)
                     }
                  }
               }
            }
         }
      } else {
         minkowski {
            translate(y = if (isFluentToSouth) { -keyPitch.y } else { 0.mm }) {
               cube(
                  0.001.mm,
                  0.001.mm +
                        if (isFluentToNorth) { keyPitch.y } else { 0.mm } +
                        if (isFluentToSouth) { keyPitch.y } else { 0.mm },
                  0.001.mm
               )
            }

            translate(z = keycapHeight) {
               rotateForTilt {
                  translate(z = dishR) {
                     sphere(dishR, fa)
                  }
               }
            }
         }
      }
   }

   fun ScadParentObject.legend(text: String): ScadObject {
      return intersection {
         translate(z = keycapHeight) {
            rotateForTilt {
               translate(z = (-0.5).mm) {
                  linearExtrude(8.mm) {
                     text(
                        text, size = 6.mm,
                        fontName = "Cica", HAlign.CENTER, VAlign.CENTER,
                        Direction.LEFT_TO_RIGHT
                     )
                  }
               }
            }
         }

         dish(keycapHeight - 0.5.mm, dishFa)
      }
   }

   fun ScadParentObject.outer(children: ScadWriter.() -> Unit): ScadObject {
      fun ScadParentObject.roundRectPyramid(): ScadObject {
         fun dishPosition(x: Point, y: Point): Point3d {
            return rotatePointForTilt(Point3d(
                     x, y,
                     z = Point(
                           dishR * (1 - sin(acos(x.distanceFromOrigin, dishR))) +
                           dishR * (1 - sin(acos(y.distanceFromOrigin, dishR)))
                     )
                  )) +
                  Size3d(0.mm, 0.mm, keycapHeight)
         }

         fun extractXy(point: Point3d) = Point2d(point.x, point.y)

         fun ScadParentObject.polygonFrom3d(points: List<Point3d>): ScadObject {
            return linearExtrude(0.1.mm) {
               polygon(points.map { extractXy(it) })
            }
         }

         val bottomR = 0.mm
         val topR = 3.mm

         val bottomPoints = listOf(
            Point3d(bottomNorthLeftX  + bottomR, bottomNorthY - bottomR, bottomZ),
            Point3d(bottomNorthRightX - bottomR, bottomNorthY - bottomR, bottomZ),
            Point3d(bottomSouthRightX - bottomR, bottomSouthY + bottomR, bottomZ),
            Point3d(bottomSouthLeftX  + bottomR, bottomSouthY + bottomR, bottomZ)
         )

         val topPoints = listOf(
            dishPosition(Point(-(topW / 2 - topR)), Point( (topH / 2 - topR))),
            dishPosition(Point( (topW / 2 - topR)), Point( (topH / 2 - topR))),
            dishPosition(Point( (topW / 2 - topR)), Point(-(topH / 2 - topR))),
            dishPosition(Point(-(topW / 2 - topR)), Point(-(topH / 2 - topR)))
         )

         val topZ = maxOf(
            dishPosition(Point(-topW / 2), Point( topH / 2)).z,
            dishPosition(Point( topW / 2), Point( topH / 2)).z,
            dishPosition(Point( topW / 2), Point(-topH / 2)).z,
            dishPosition(Point(-topW / 2), Point(-topH / 2)).z
         )

         fun getRate(z: Point) = (z - bottomZ).numberAsMilliMeter / (topZ - bottomZ).numberAsMilliMeter
         fun rAt(z: Point) = bottomR + (topR - bottomR) * getRate(z)

         return union {
            val step = 0.5.mm
            for (z in bottomZ..topZ step step) {
               hull {
                  translate(z = z.distanceFromOrigin) {
                     minkowski {
                        polygonFrom3d(
                           (bottomPoints zip topPoints)
                              .map { (b, t) -> zPointOnLine(b, t, z) }
                        )

                        cylinder(height = 0.001.mm, radius = rAt(z), keycapWallFa)
                     }
                  }

                  translate(z = z.distanceFromOrigin + step) {
                     minkowski {
                        polygonFrom3d(
                           (bottomPoints zip topPoints)
                              .map { (b, t) -> zPointOnLine(b, t, z + step) }
                        )

                        cylinder(height = 0.001.mm, radius = rAt(z + step), keycapWallFa)
                     }
                  }
               }
            }
         }
      }

      return intersection {
         difference {
            roundRectPyramid()
            dish(keycapHeight, dishFa)
         }

         children()
      }
   }

   fun ScadParentObject.inner(): ScadObject {
      fun ScadParentObject.rectPyramid(): ScadObject {
         return hull {
            val topZ = Point(keycapHeight)

            translate(z = topZ.distanceFromOrigin) {
               rotateForTilt {
                  cube(
                     topW - Keycap.THICKNESS * 2,
                     topH - Keycap.THICKNESS * 2,
                     0.01.mm,
                     center = true
                  )
               }
            }

            translate(z = bottomZ.distanceFromOrigin) {
               linearExtrude(0.01.mm) {
                  polygon(listOf(
                     Point2d(bottomNorthLeftX  + Keycap.THICKNESS, bottomNorthY - Keycap.THICKNESS),
                     Point2d(bottomNorthRightX - Keycap.THICKNESS, bottomNorthY - Keycap.THICKNESS),
                     Point2d(bottomSouthRightX - Keycap.THICKNESS, bottomSouthY + Keycap.THICKNESS),
                     Point2d(bottomSouthLeftX  + Keycap.THICKNESS, bottomSouthY + Keycap.THICKNESS)
                  ))
               }
            }
         }
      }

      return difference {
         rectPyramid()
         dish(keycapHeight - Keycap.THICKNESS, keycapInvisibleFa)
      }
   }

   fun ScadParentObject.pillar(children: ScadWriter.() -> Unit): ScadObject {
      return intersection {
         union {
            translate((-16).mm, (- 1.5).mm, 2.5.mm) { cube(32.mm,  3.mm, 24.mm) }
            translate((- 1).mm, (-16.0).mm, 2.5.mm) { cube( 2.mm, 32.mm, 24.mm) }

            translate(z = 2.5.mm) {
               polygonPyramid(
                  16,
                  height = if (isThinPillar) { 2.mm } else { 24.mm },
                  radius = 4.3.mm
               )
            }

            val northSouthThickness = 1.05.mm + polishingMargin
            val eastWestThickness   = 1.25.mm + polishingMargin

            translate(z = 12.5.mm) { cube(northSouthThickness, 4.mm, 24.mm, center = true) }
            translate(z = 12.5.mm) { cube(4.mm, eastWestThickness,   24.mm, center = true) }
         }

         union {
            outer { children() }

            difference {
               translate(z = (-3).mm) { polygonPyramid(16, height = 24.mm, radius = 4.3.mm) }
               dish(keycapHeight - Keycap.THICKNESS, keycapInvisibleFa)
            }
         }
      }
   }

   fun ScadParentObject.homePositionMark(): ScadObject {
      return translate(z = keycapHeight) {
         rotateForTilt {
            translate((-0.5).mm, -topH / 2 + 0.15.mm, (-1.5).mm) {
               minkowski {
                  cube(1.mm, 0.001.mm, 2.75.mm)
                  sphere(0.3.mm, fa = 12.0)
               }
            }
         }
      }
   }

   return difference {
      if (enableOnlyOuter) {
         outer { children() }
      } else {
         union {
            difference {
               union {
                  outer { children() }

                  if (isHomePosition) {
                     homePositionMark()
                  }
               }

               inner()
            }

            pillar { children() }
         }
      }

      if (enableLegends) {
         legend(legend)
      }
   }
}
