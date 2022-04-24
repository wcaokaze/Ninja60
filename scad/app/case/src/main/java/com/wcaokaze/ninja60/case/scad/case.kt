package com.wcaokaze.ninja60.case.scad

import com.wcaokaze.ninja60.case.*
import com.wcaokaze.ninja60.parts.key.*
import com.wcaokaze.ninja60.parts.key.alphanumeric.*
import com.wcaokaze.ninja60.parts.rotaryencoder.*
import com.wcaokaze.ninja60.shared.*
import com.wcaokaze.ninja60.shared.calcutil.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

fun ScadParentObject.case(case: Case): ScadObject {
   var scad: ScadObject

   val baseCase = memoize {
      union {
         alphanumericCase(case, otherOffsets = 1.5.mm)

         frontRotaryEncoderKnobCase(
            case,
            radiusOffset = PrinterAdjustments.minWallThickness.value
         )

         frontRotaryEncoderKeyCase(
            case.frontRotaryEncoderKey,
            height = Case.FRONT_ROTARY_ENCODER_KEY_CASE_HEIGHT
                  + PrinterAdjustments.minWallThickness.value,
            offset = PrinterAdjustments.minWallThickness.value
         )

         thumbHomeKeyHole(
            case.thumbHomeKey,
            height = KeySwitch.TRAVEL,
            bottomOffset = Case.THUMB_HOME_KEY_CASE_HEIGHT
                  + PrinterAdjustments.minWallThickness.value,
            frontOffset = PrinterAdjustments.minWallThickness.value,
            backOffset = 20.mm,
            leftOffset = 20.mm,
            rightOffset = PrinterAdjustments.minWallThickness.value
         )
      }
   }

   scad = baseCase()

   scad += wristRest(
      case,
      alphanumericLeftPlane(case.alphanumericPlate, offset = 1.5.mm),
      alphanumericFrontSlopePlane(case.alphanumericPlate, offset = 1.5.mm),
      alphanumericFrontPlaneLeft(case.alphanumericPlate, offset = 1.5.mm),
      alphanumericBottomPlane(case, offset = 0.mm)
   )

   scad -= union {
      alphanumericCase(case, bottomOffset = 1.5.mm)
      //thumbCase(case)
      frontRotaryEncoderKnobCase(case)
      frontRotaryEncoderKeyCase(case.frontRotaryEncoderKey,
         height = Case.FRONT_ROTARY_ENCODER_KEY_CASE_HEIGHT)
      thumbHomeKeyHole(case.thumbHomeKey, height = 0.mm, leftOffset = 20.mm,
         bottomOffset = Case.THUMB_HOME_KEY_CASE_HEIGHT, backOffset = 20.mm)
   }


   // ==== alphanumericのプレート部 ============================================

   // キースイッチの底の高さでhull。alphanumericCaseに確実に引っ付けるために
   // 前後左右広めに生成してalphanumericCaseとのintersectionをとります
   scad += intersection {
      baseCase()
      hullAlphanumericPlate(
         case.alphanumericPlate,
         HullAlphanumericConfig(
            layerOffset = KeySwitch.BOTTOM_HEIGHT,
            frontBackOffset = 20.mm,
            leftRightOffset = 20.mm,
            columnOffset = 1.mm
         )
      )
   }

   val alphanumericHollow = memoize {
      union {
         // プレートの表面の高さでhull
         hullAlphanumericPlate(case.alphanumericPlate, HullAlphanumericConfig())

         // キーキャップの底の高さ(プレートの表面からキースイッチのストローク分
         // 高い位置)で前後左右広めにhull。こうすることでスイッチを押し込んでないときの
         // 高さとスイッチの押し込んだときの高さの二段の形状になっておしゃれです
         hullAlphanumericPlate(
            case.alphanumericPlate,
            HullAlphanumericConfig(
               layerOffset = -KeySwitch.TRAVEL,
               frontBackOffset = 20.mm,
               leftRightOffset = 20.mm
            )
         )
      }
   }

   scad -= alphanumericHollow()


   // ==== thumbのプレート部 ===================================================

   scad += thumbHomeKeyHole(case.thumbHomeKey,
      height = KeySwitch.TRAVEL,
      bottomOffset = KeySwitch.BOTTOM_HEIGHT,
      frontOffset = PrinterAdjustments.minWallThickness.value,
      backOffset = PrinterAdjustments.minWallThickness.value,
      leftOffset = PrinterAdjustments.minWallThickness.value,
      rightOffset = PrinterAdjustments.minWallThickness.value)
   scad -= thumbHomeKeyHole(case.thumbHomeKey, height = 100.mm)

   scad += thumbPlateCase(case.thumbPlate,
      height = KeySwitch.TRAVEL,
      bottomOffset = KeySwitch.BOTTOM_HEIGHT,
      otherOffsets = PrinterAdjustments.minWallThickness.value)
   scad -= thumbPlateCase(case.thumbPlate, height = 100.mm)


   // ==== 奥側ロータリーエンコーダ ============================================

   /*
   scad += (
      backRotaryEncoderCase(case, otherOffsets = 1.5.mm)
      - backRotaryEncoderCase(case, bottomOffset = 1.5.mm, frontOffset = (-1.5).mm)
      - baseCase()
      + backRotaryEncoderKnobHolder(case)
      - backRotaryEncoderKnobCave(case)
   )

   scad -= backRotaryEncoderInsertionHole(case)
   scad += backRotaryEncoderMountPlate(case)
   */


   // ==== 手前側ロータリーエンコーダ ==========================================

   scad += (
         union {
            frontRotaryEncoderKnobHole(case.frontRotaryEncoderKnob,
               bottomOffset = PrinterAdjustments.minWallThickness.value,
               radiusOffset = PrinterAdjustments.minWallThickness.value)
            frontRotaryEncoderHole(case.frontRotaryEncoderKnob.rotaryEncoder,
               bottomOffset = 1.6.mm, otherOffsets = 1.5.mm)
            frontRotaryEncoderKeyHole(case.frontRotaryEncoderKey,
               height = KeySwitch.TRAVEL,
               bottomOffset = KeySwitch.BOTTOM_HEIGHT,
               innerRadiusOffset = PrinterAdjustments.minWallThickness.value,
               otherOffsets = PrinterAdjustments.minWallThickness.value)
         }
               intersection hugeCube(
            topPlane = alphanumericTopPlaneLeft(
               case.alphanumericPlate,
               offset = PrinterAdjustments.minWallThickness.value
                     + PrinterAdjustments.minHollowSize.value.z
            )
         )

               // 手前側ロータリーエンコーダは意図的にalphanumericとカブる位置に配置されてます
               // alphanumeric部分にはみ出た分を削ります
               - alphanumericHollow()
         )

   scad -= union {
      frontRotaryEncoderKnobHole(case.frontRotaryEncoderKnob)
      frontRotaryEncoderHole(case.frontRotaryEncoderKnob.rotaryEncoder)
      frontRotaryEncoderKeyHole(case.frontRotaryEncoderKey, height = 100.mm)
      frontRotaryEncoderKeyHole(
         case.frontRotaryEncoderKey,
         height = 100.mm,
         bottomOffset = frontRotaryEncoderKnobHoleZOffset()
               - Case.FRONT_ROTARY_ENCODER_KEY_Z_OFFSET,
         innerRadiusOffset = 3.mm)
      rotaryEncoderMountHole(case.frontRotaryEncoderKnob.rotaryEncoder, 2.mm)
   }

   // alphanumericの壁、ノブが配置されてる列をごっそり削ります
   scad -= intersection {
      frontRotaryEncoderKnobHole(case.frontRotaryEncoderKnob, radiusOffset = 100.mm)

      hullColumn(
         case.alphanumericPlate.columns[Case.FRONT_ROTARY_ENCODER_COLUMN_INDEX],
         case.alphanumericPlate.columns.getOrNull(Case.FRONT_ROTARY_ENCODER_COLUMN_INDEX - 1),
         case.alphanumericPlate.columns.getOrNull(Case.FRONT_ROTARY_ENCODER_COLUMN_INDEX + 1),
         HullAlphanumericConfig(
            layerOffset = 20.mm,
            frontBackOffset = 40.mm,
            columnOffset = 1.mm
         )
      )
   }


   // ==== スイッチ穴 ==========================================================

   val allSwitches = case.alphanumericPlate.columns.flatMap { it.keySwitches } +
         case.thumbHomeKey +
         case.thumbPlate.keySwitches + case.frontRotaryEncoderKey.switch

   val switchHole = memoize { switchHole() }
   val switchSideHolder = memoize { switchSideHolder() }

   scad -= union {
      for (s in allSwitches) {
         place(s) { switchHole() }
      }
   }

   scad += union {
      for (s in allSwitches) {
         place(s) { switchSideHolder() }
      }
   }

   return scad
}
