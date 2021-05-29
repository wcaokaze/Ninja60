package com.wcaokaze.ninja60

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*
import java.io.File

/**
 * デバッグ用。キーキャップに透明のステムホルダーが挿さった状態のモデルを生成します
 *
 * @param x
 * キーの東西方向の位置。
 * @param y
 * キーの南北方向の位置。
 * @param caseX
 * ケースの原点に対してキーが配置される位置。
 * あえてxと一致させないことでx == 0のキーを東端に配置することなどが可能
 * @param caseY
 * ケースの原点に対してキーが配置される位置。
 * あえてyと一致させないことでy == 0のキーを南端に配置することなどが可能
 */
private fun ScadWriter.keycapWithStem(
   x: Double, y: Double, caseX: Point, caseY: Point,
   w: Double = 1.0, h: Double = 1.0, legend: String = "",
   isFluentToNorth: Boolean = false, isFluentToSouth: Boolean = false,
   leftWallPadding: Size = 0.mm, rightWallPadding: Size = 0.mm,
   leftWallAngle: Angle = 0.0.rad, rightWallAngle: Angle = 0.0.rad, wallY: Point = Point(0.mm),
   isCylindrical: Boolean = false, isHomePosition: Boolean = false,
   isThinPillar: Boolean = false
) {
   keycap(
      x, y, w, h, legend, isFluentToNorth, isFluentToSouth,
      isCylindrical, isHomePosition, isThinPillar,
      bottomZ = caseCurveZ(
         caseX closeOrigin (keyPitchH * 0.5),
         caseY - keyPitchV * 0.5
      ),
      leftWallPadding, rightWallPadding, leftWallAngle, rightWallAngle,
      wallY
   ) {
      translate(-caseX.distanceFromOrigin, -caseY.distanceFromOrigin) { caseCurve() }
   }

   translate(z = (-3).mm) { stemHolder() }
   translate(z = 0.5.mm) { oRing() }
}

fun ScadWriter.leftKeys() {
   val keycapHalfWidth = keyPitchH / 2 - keycapMargin

   use(File("src/main/res/Cica-Regular.ttf"))
   prepareStemHolderModule()
   prepareORingModule()

   // x = -2
   translate(
      -keycapHalfWidth * cos(0.deg) - keyPitchH * cos(3.deg) - (keycapMargin - 1.mm) * 2 * cos(3.deg),
      -keycapHalfWidth * sin(0.deg) - keyPitchH * sin(3.deg) - (keycapMargin - 1.mm) * 2 * sin(3.deg)
   ) {
      rotate(z = 3.deg) {
         val s = (-4).mm
         for (y in -1..2) {
            val caseX = Point(keyPitchH * -2)
            val caseY = Point(keyPitchV * (y + 2) + s)
            translate(-keycapHalfWidth, caseY.distanceFromOrigin) {
               keycapWithStem(
                  -2.0, y.toDouble(), caseX, caseY,
                  rightWallPadding = (-2).mm,
                  isThinPillar = false
               )
            }
         }
      }
   }

   // x = -1
   translate(
      -keycapHalfWidth * cos(0.deg) - keycapMargin * 2 * cos(3.deg),
      -keycapHalfWidth * sin(0.deg) - keycapMargin * 2 * sin(3.deg)
   ) {
      rotate(z = 3.deg) {
         val s = (-1).mm
         val legends = listOf("Z", "A", "'", "1")
         for (y in -1..2) {
            val caseX = Point(keyPitchH * -1)
            val caseY = Point(keyPitchV * (y + 2) + s)
            translate(-keycapHalfWidth, caseY.distanceFromOrigin) {
               keycapWithStem(
                  -1.0, y.toDouble(), caseX, caseY, legend = legends[y + 1],
                  rightWallAngle = (-1.5).deg,
                  wallY = caseY,
                  isThinPillar = false
               )
            }
         }
      }
   }

   // x = 0
   translate(
      keycapHalfWidth * cos(0.deg),
      keycapHalfWidth * sin(0.deg)
   ) {
      rotate(z = 0.deg) {
         val s = 9.5.mm
         val legends = listOf("Q", "O", ",", "2")
         for (y in -1..2) {
            val caseX = Point(keyPitchH * 0)
            val caseY = Point(keyPitchV * (y + 2) + s)
            translate(-keycapHalfWidth, caseY.distanceFromOrigin) {
               keycapWithStem(
                  0.0, y.toDouble(), caseX, caseY, legend = legends[y + 1],
                  leftWallAngle = 1.5.deg,
                  rightWallAngle = (-1).deg,
                  wallY = caseY,
                  isThinPillar = false
               )
            }
         }
      }
   }

   // x = 1
   translate(
      keycapHalfWidth * cos(0.deg) + keycapMargin * 2 * cos((-2).deg),
      keycapHalfWidth * sin(0.deg) + keycapMargin * 2 * sin((-2).deg)
   ) {
      rotate(z = (-2).deg) {
         val s = 14.25.mm
         val legends = listOf("J", "E", ".", "3")
         for (y in -1..2) {
            val caseX = Point(keyPitchH * 1)
            val caseY = Point(keyPitchV * (y + 2) + s)
            translate(keycapHalfWidth, caseY.distanceFromOrigin) {
               keycapWithStem(
                  1.0, y.toDouble(), caseX, caseY, legend = legends[y + 1],
                  leftWallAngle = 1.deg,
                  wallY = caseY,
                  isThinPillar = false
               )
            }
         }
      }
   }

   // x = 2
   translate(
      keycapHalfWidth * cos(0.deg) + keyPitchH * cos((-2).deg) + 2.mm + keycapMargin * 2 * cos((-4).deg),
      keycapHalfWidth * sin(0.deg) + keyPitchH * sin((-2).deg)        + keycapMargin * 2 * sin((-4).deg)
   ) {
      rotate(z = (-6).deg) {
         val s = 4.mm
         val legends = listOf("K", "U", "P", "4")
         for (y in -1..2) {
            val caseX = Point(keyPitchH * 2)
            val caseY = Point(keyPitchV * (y + 2) + s)
            translate(keycapHalfWidth, caseY.distanceFromOrigin) {
               keycapWithStem(
                  2.0, y.toDouble(), caseX, caseY, legend = legends[y + 1],
                  wallY = caseY,
                  isFluentToNorth = (y == 0),
                  isFluentToSouth = (y == 1),
                  isHomePosition = (y == 0),
                  isThinPillar = false
               )
            }
         }
      }
   }

   // x = 3
   translate(
      keycapHalfWidth * cos(0.deg) + keyPitchH * cos((-2).deg) + 2.mm + keyPitchH * cos((-4).deg) + (keycapMargin - 1.mm) * 2 * cos((-4).deg),
      keycapHalfWidth * sin(0.deg) + keyPitchH * sin((-2).deg)        + keyPitchH * sin((-4).deg) + (keycapMargin - 1.mm) * 2 * sin((-4).deg)
   ) {
      rotate(z = (-6).deg) {
         val legends = listOf("X", "I", "Y", "5")
         for (y in -1..2) {
            val caseX = Point(keyPitchH * 3)
            val caseY = Point(keyPitchV * (y + 2))
            translate(keycapHalfWidth, caseY.distanceFromOrigin) {
               keycapWithStem(
                  3.0, y.toDouble(), caseX, caseY, legend = legends[y + 1],
                  leftWallPadding = (-2).mm,
                  wallY = caseY,
                  isFluentToNorth = (y == 0),
                  isFluentToSouth = (y == 1),
                  isThinPillar = true
               )
            }
         }
      }
   }

   translate(14.mm, 7.mm) { encoderKnob() }

   translate(16.mm, (-63).mm) {
      rotate(z = 59.deg) {
         rotate(z = 15.deg * -2) { translate(x = 73.mm) { thumbKeycap(73.mm, (-7.5).deg / 2, 7.5.deg / 2, h = 16.mm, dishOffset =   0 .mm) } }
         rotate(z = 15.deg * -1) { translate(x = 73.mm) { thumbKeycap(73.mm, (-7.5).deg / 2, 7.5.deg / 2, h = 16.mm, dishOffset =   0 .mm) } }
         rotate(z = 15.deg *  0) { translate(x = 73.mm) { thumbKeycap(73.mm, (-7.5).deg / 2, 7.5.deg / 2, h = 24.mm, dishOffset = (-1).mm) } }
         rotate(z = 15.deg *  1) { translate(x = 73.mm) { thumbKeycap(73.mm, (-7.5).deg / 2, 7.5.deg / 2, h = 16.mm, dishOffset = (-2).mm) } }
      }
   }
}
