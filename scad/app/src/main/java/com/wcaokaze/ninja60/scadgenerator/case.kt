package com.wcaokaze.ninja60.scadgenerator

import com.wcaokaze.ninja60.scadgenerator.scadwriter.*
import com.wcaokaze.ninja60.scadgenerator.scadwriter.foundation.*

val caseFa = 2.0

val caseCurveR = 850.mm
val caseSouthR = 380.mm
val caseNorthR = 255.mm

val caseSouthX = 16.mm * 1
val caseNorthX = 16.mm * 0

val caseStartAngle = (-90).deg
val caseEndAngle   = (-80).deg

private fun interpolate(start: Angle, end: Angle, rate: Double) = start + (end - start) * rate
private fun interpolate(start: Size,  end: Size,  rate: Double) = start + (end - start) * rate

/**
 * キーボードに対して垂直に東西方向に立てた半径case_south_rの円を、
 * キーボードに対して垂直に南北方向に立てた半径case_curve_rの円弧上を
 * 走らせた場合に残る残像の形状です。
 *
 * このとき、移動する円の半径は徐々にcase_south_rからcase_north_rに変化し、
 * x座標は徐々にcase_south_xからcase_north_xに移動します。
 *
 * 具体的には
 * 南端に立てた円が徐々に小さくなりながら西に移動しながら円弧を描いて北に移動しています
 * 無茶苦茶ですね
 */
fun ScadWriter.caseCurve() {
   val step = 0.1

   translate(0.mm, keyPitchV * -0.5, caseCurveR) {
      union {
         val seq = sequence {
            var i = 0.0
            while (i <= 1.0) {
               yield(i)
               i += step
            }
         }

         for (i in seq) {
            val aAngle = interpolate(caseStartAngle, caseEndAngle, i)
            val aY = caseCurveR * cos(aAngle)
            val aZ = caseCurveR * sin(aAngle)

            val bAngle = interpolate(caseStartAngle, caseEndAngle, i + step)
            val bY = caseCurveR * cos(bAngle)
            val bZ = caseCurveR * sin(bAngle)

            val aR = interpolate(caseSouthR, caseNorthR, i)
            val bR = interpolate(caseSouthR, caseNorthR, i + step)

            val aX = interpolate(caseSouthX, caseNorthX, i)
            val bX = interpolate(caseSouthX, caseNorthX, i + step)

            hull {
               translate(aX, aY, aZ + aR) { rotate(x = 90.deg) { cylinder(height = 0.01.mm, radius = aR, caseFa) } }
               translate(bX, bY, bZ + bR) { rotate(x = 90.deg) { cylinder(height = 0.01.mm, radius = bR, caseFa) } }
            }
         }
      }
   }
}

fun caseYToAngle(y: Point): Angle = atan(-caseCurveR, y.distanceFromOrigin + keyPitchV * 0.5)

fun caseYToInterpolateRate(y: Point): Double
      = (caseYToAngle(y) - caseStartAngle).numberAsRadian / (caseEndAngle - caseStartAngle).numberAsRadian

fun caseYToCylinderR(y: Point): Size
      = interpolate(caseSouthR, caseNorthR, caseYToInterpolateRate(y))

fun casePosToCylinderAngle(x: Point, y: Point): Angle
      = atan(-caseYToCylinderR(y),
         x.distanceFromOrigin - interpolate(caseSouthX, caseNorthX, caseYToInterpolateRate(y))
      )

/**
 * case_curveの(x, y)におけるz座標。
 *
 * @param x
 * 東西方向の位置。
 * @param y
 * 南北方向の位置。
 */
fun caseCurveZ(x: Point, y: Point): Point
      = Point.ORIGIN +
      caseCurveR * (1 + sin(caseYToAngle(y))) +
      caseYToCylinderR(y) * (1 + sin(casePosToCylinderAngle(x, y)))
