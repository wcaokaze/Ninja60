package com.wcaokaze.ninja60.case

/*
fun backRotaryEncoderCaseTopPlane(
   case: Case,
   offset: Size
): Plane3d {
   val gear = case.backRotaryEncoderGear.spurGear

   return Plane3d(
         gear.referencePoint,
         gear.bottomVector vectorProduct case.backVector
      )
      .translateTangential(gear)
      .let { it.translate(it.normalVector, offset + 0.2.mm) }
}

fun backRotaryEncoderCaseBottomPlane(case: Case, offset: Size): Plane3d
      = alphanumericBottomPlane(case, offset)

fun backRotaryEncoderCaseLeftPlane(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d {
   val column = alphanumericPlate.columns[BackRotaryEncoderKnob.COLUMN_INDEX]

   return Plane3d(column.referencePoint, column.leftVector)
      .translate(column.leftVector, BackRotaryEncoderMediationGear.CASE_WIDTH / 2 + offset)
}

fun backRotaryEncoderCaseRightPlane(alphanumericPlate: AlphanumericPlate, offset: Size): Plane3d {
   val column = alphanumericPlate.columns[BackRotaryEncoderKnob.COLUMN_INDEX]

   return Plane3d(column.referencePoint, column.rightVector)
      .translate(column.rightVector, BackRotaryEncoderMediationGear.CASE_WIDTH / 2 + offset)
}

fun backRotaryEncoderCaseSlopePlane(
   alphanumericPlate: AlphanumericPlate,
   gear: Gear,
   offset: Size
): Plane3d {
   return alphanumericBackSlopePlane(alphanumericPlate, 1.5.mm)
      .translateTangential(gear)
      .let { it.translate(it.normalVector, offset + 0.2.mm) }
}

fun backRotaryEncoderCaseFrontPlane(case: Case, offset: Size): Plane3d {
   return Plane3d(
         case.backRotaryEncoderGear.referencePoint,
         -alphanumericBackPlane(case, 0.0.mm).normalVector
      )
      .translateTangential(case.backRotaryEncoderGear.gear)
      .let { it.translate(-it.normalVector, offset + 0.2.mm) }
}

fun backRotaryEncoderCaseBackPlane(
   case: Case,
   offset: Size
): Plane3d {
   return alphanumericBackPlane(case, 0.mm)
      .translateTangential(case.backRotaryEncoderGear.gear)
      .let { it.translate(it.normalVector, offset + 0.2.mm) }
}

fun ScadParentObject.backRotaryEncoderCase(
   case: Case,
   bottomOffset: Size = 0.mm,
   frontOffset: Size = 0.mm,
   otherOffsets: Size = 0.mm
): ScadObject {
   val caseLeftPlane  = backRotaryEncoderCaseLeftPlane (case.alphanumericPlate, otherOffsets)
   val caseRightPlane = backRotaryEncoderCaseRightPlane(case.alphanumericPlate, otherOffsets)

   val caseBackPlane = backRotaryEncoderCaseBackPlane(case, otherOffsets)
   val caseBackSlopePlane = backRotaryEncoderCaseSlopePlane(case.alphanumericPlate, case.backRotaryEncoderGear.gear, otherOffsets)
   val caseTopPlane = backRotaryEncoderCaseTopPlane(case, otherOffsets)
   val caseFrontPlane = backRotaryEncoderCaseFrontPlane(case, frontOffset)
   val caseBottomPlane = backRotaryEncoderCaseBottomPlane(case, bottomOffset)

   return hullPoints(
      listOf(caseLeftPlane, caseRightPlane).flatMap { a ->
         listOf(
               caseBackPlane, caseBackSlopePlane, caseTopPlane, caseFrontPlane,
               caseBottomPlane, caseBackPlane
            )
            .zipWithNext()
            .map { (b, c) ->
               a intersection b intersection c
            }
      }
   )
}

fun ScadParentObject.backRotaryEncoderInsertionHole(case: Case): ScadObject {
   return backRotaryEncoderCase(case, frontOffset = 2.mm)
}

fun ScadParentObject.backRotaryEncoderMountPlate(case: Case): ScadObject {
   return difference {
      val rotaryEncoder = case.backRotaryEncoderGear.rotaryEncoder

      intersection {
         backRotaryEncoderCase(case, otherOffsets = 1.5.mm)

         cube(Cube(
            rotaryEncoder.referencePoint
               .translate(rotaryEncoder.leftVector,   10.0.mm)
               .translate(rotaryEncoder.frontVector,  50.0.mm)
               .translate(rotaryEncoder.bottomVector,  1.6.mm),
            Size3d(30.mm, 100.mm, 1.6.mm),
            rotaryEncoder.frontVector,
            rotaryEncoder.bottomVector
         ))
      }

      rotaryEncoderMountHole(rotaryEncoder, 2.mm)
      hullAlphanumericPlate(case.alphanumericPlate, HullAlphanumericConfig())
   }
}

fun ScadParentObject.backRotaryEncoderKnobHolder(case: Case): ScadObject {
   val topPlane = backRotaryEncoderCaseTopPlane(case, 0.mm)
      .translate(case.backRotaryEncoderKnob.referencePoint)

   val frontPlane = Plane3d(
      case.backRotaryEncoderKnob.referencePoint,
      case.backRotaryEncoderKnob.topVector vectorProduct topPlane.normalVector
   )

   val backPlane = backRotaryEncoderCaseBackPlane(case, 0.mm)

   val rightPlane = backRotaryEncoderCaseRightPlane(case.alphanumericPlate, 0.mm)
      .translate(case.backRotaryEncoderKnob.referencePoint)
      .translate(case.backRotaryEncoderKnob.topVector, BackRotaryEncoderKnob.HEIGHT)

   val leftPlane = backRotaryEncoderCaseLeftPlane(case.alphanumericPlate, 0.mm)
      .translate(case.backRotaryEncoderKnob.referencePoint)
      .translate(case.backRotaryEncoderKnob.bottomVector, BackRotaryEncoderKnob.GEAR_THICKNESS)

   return minkowski {
      hullPoints(
         listOf(backPlane, rightPlane, frontPlane, leftPlane, backPlane)
            .zipWithNext()
            .map { (a, b) -> a intersection b intersection topPlane }
      )

      rotate(
         -Vector3d.Z_UNIT_VECTOR angleWith case.backRotaryEncoderKnob.bottomVector,
         -Vector3d.Z_UNIT_VECTOR vectorProduct case.backRotaryEncoderKnob.bottomVector,
      ) {
         cylinder(
            height = 6.mm,
            radius = 4.mm,
            center = true,
            `$fa`
         )
      }
   } intersection distortedCube(
      backRotaryEncoderCaseSlopePlane(case.alphanumericPlate, case.backRotaryEncoderGear.gear, 1.5.mm),
      backRotaryEncoderCaseLeftPlane(case.alphanumericPlate, 100.mm),
      backRotaryEncoderCaseBackPlane(case, 1.5.mm),
      backRotaryEncoderCaseRightPlane(case.alphanumericPlate, 100.mm),
      Plane3d(case.referencePoint, case.frontVector),
      Plane3d(case.referencePoint, case.topVector)
   )
}

fun ScadParentObject.backRotaryEncoderKnobCave(case: Case): ScadObject {
   val knob = case.backRotaryEncoderKnob

   return place(knob) {
      translate(z = -BackRotaryEncoderKnob.GEAR_THICKNESS) {
         translate(z = (-0.6).mm) {
            cylinder(
               BackRotaryEncoderKnob.HEIGHT
                     + BackRotaryEncoderKnob.GEAR_THICKNESS
                     + 1.2.mm,
               BackRotaryEncoderKnob.RADIUS
                     + BackRotaryEncoderKnob.SKIDPROOF_RADIUS / 2
                     + 0.7.mm,
               `$fa`
            )
         }

         translate(z = (-1.5).mm) {
            cylinder(
               30.mm,
               BackRotaryEncoderKnob.SHAFT_HOLE_RADIUS,
               `$fa`
            )
         }
      }
   }
}
*/
