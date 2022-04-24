package com.wcaokaze.ninja60.case

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.ninja60.shared.scadutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

fun ScadParentObject.wristRest(
   case: Case,
   alphanumericCaseLeftPlane: Plane3d,
   alphanumericCaseFrontSlopePlane: Plane3d,
   alphanumericCaseFrontPlane: Plane3d,
   alphanumericCaseBottomPlane: Plane3d
): ScadObject {
   return union {
      val bridgeTopPlane = wristRestBridgeTopPlane(case, alphanumericCaseLeftPlane,
         alphanumericCaseFrontSlopePlane, alphanumericCaseFrontPlane)
      val bridgeBottomPlane = wristRestBridgeBottomPlane(alphanumericCaseBottomPlane)
      val bridgeLeftPlane = wristRestBridgeLeftPlane(alphanumericCaseLeftPlane)
      val bridgeRightPlane = wristRestBridgeRightPlane(case)
      val bridgeBackPlane = wristRestBridgeBackPlane(alphanumericCaseFrontPlane)
      val bridgeFrontPlane = wristRestBridgeFrontPlane(case)

      hullPoints(
         listOf(bridgeLeftPlane, bridgeRightPlane).flatMap { x ->
            listOf(bridgeFrontPlane, bridgeBackPlane).flatMap { y ->
               listOf(bridgeBottomPlane, bridgeTopPlane).map { z ->
                  x intersection y intersection z
               }
            }
         }
      )

      val topPlane = wristRestTopPlane(case)
      val bottomPlane = wristRestBottomPlane(alphanumericCaseBottomPlane)
      val leftPlane = wristRestLeftPlane(case, alphanumericCaseLeftPlane,
         alphanumericCaseFrontSlopePlane, alphanumericCaseFrontPlane)
      val rightPlane = wristRestRightPlane(case, alphanumericCaseLeftPlane,
         alphanumericCaseFrontSlopePlane, alphanumericCaseFrontPlane)
      val backPlane = wristRestBackPlane(case)
      val frontPlane = wristRestFrontPlane(case)

      hullPoints(
         listOf(leftPlane, rightPlane).flatMap { x ->
            listOf(frontPlane, backPlane).flatMap { y ->
               listOf(bottomPlane, topPlane).map { z ->
                  x intersection y intersection z
               }
            }
         }
      )
   }
}

private fun wristRestBridgeLeftPlane(alphanumericCaseLeftPlane: Plane3d)
      = alphanumericCaseLeftPlane

private fun wristRestBridgeRightPlane(
   case: Case
): Plane3d {
   val normalVector = case.thumbHomeKey.rightVector vectorProduct case.backVector

   return Plane3d(
      case.thumbHomeKey.referencePoint,
      normalVector
   )
}

private fun wristRestBridgeFrontPlane(case: Case) = wristRestFrontPlane(case)

private fun wristRestBridgeBackPlane(alphanumericCaseFrontPlane: Plane3d)
      = alphanumericCaseFrontPlane

private fun wristRestBridgeTopPlane(
   case: Case,
   alphanumericCaseLeftPlane: Plane3d,
   alphanumericCaseFrontSlopePlane: Plane3d,
   alphanumericCaseFrontPlane: Plane3d
): Plane3d {
   val caseLeftFrontTop = (
         alphanumericCaseLeftPlane
               intersection alphanumericCaseFrontSlopePlane
               intersection alphanumericCaseFrontPlane
   )

   return Plane3d(
      caseLeftFrontTop.translate(case.bottomVector, 9.mm),
      case.topVector
   )
}

private fun wristRestBridgeBottomPlane(alphanumericCaseBottomPlane: Plane3d)
      = alphanumericCaseBottomPlane

private fun wristRestLeftPlane(
   case: Case,
   alphanumericCaseLeftPlane: Plane3d,
   alphanumericCaseFrontSlopePlane: Plane3d,
   alphanumericCaseFrontPlane: Plane3d
): Plane3d {
   return Plane3d(
      wristRestBackPlane(case)
            intersection wristRestBridgeLeftPlane(alphanumericCaseLeftPlane)
            intersection wristRestBridgeTopPlane(
               case,
               alphanumericCaseLeftPlane,
               alphanumericCaseFrontSlopePlane,
               alphanumericCaseFrontPlane
            ),
      case.leftVector
   )
}

private fun wristRestRightPlane(
   case: Case,
   alphanumericCaseLeftPlane: Plane3d,
   alphanumericCaseFrontSlopePlane: Plane3d,
   alphanumericCaseFrontPlane: Plane3d
): Plane3d {
   return Plane3d(
      wristRestBackPlane(case)
            intersection wristRestBridgeRightPlane(case)
            intersection wristRestBridgeTopPlane(
               case,
               alphanumericCaseLeftPlane,
               alphanumericCaseFrontSlopePlane,
               alphanumericCaseFrontPlane
            ),
      case.rightVector
   )
}

private fun wristRestFrontPlane(case: Case) = Plane3d(
   case.referencePoint.translate(y = (-108).mm),
   case.frontVector
)

private fun wristRestBackPlane(case: Case) = Plane3d(
   case.referencePoint.translate(y = (-38).mm),
   case.backVector
)

private fun wristRestBottomPlane(alphanumericCaseBottomPlane: Plane3d)
      = alphanumericCaseBottomPlane

private fun wristRestTopPlane(case: Case) = Plane3d(
   case.referencePoint.translate(z = 60.mm),
   case.topVector
)
