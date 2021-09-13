package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*
import java.io.File
import kotlin.system.exitProcess

fun main(vararg args: String) {
   val config = try {
      parseArguments(args)
   } catch (e: ArgumentParseException) {
      System.err.println(e.message)
      exitProcess(1)
   }

   writeScad(config.outputFile) {
      val gear1 = Gear(3, 12, 3.mm, Point3d.ORIGIN, -Vector3d.Y_UNIT_VECTOR, -Vector3d.Z_UNIT_VECTOR)
      val gear2 = Gear(3, 16, 3.mm, Point3d.ORIGIN, -Vector3d.Y_UNIT_VECTOR, -Vector3d.Z_UNIT_VECTOR)

      gear(gear1)

      gear(gear2)
         .rotate(z = Angle.PI * 2 / gear2.toothCount / 2)
         .translate(y = gear1 distance gear2)

      /*
      mirror(1.mm, 0.mm, 0.mm) {
         val case = Case()
         case(case)
      }

      case.alphanumericPlate.columns
         .flatMap { it.keySwitches.map { it.plate(Size2d(16.mm, 16.mm)) } }
         .map { it.translate(it.topVector, KeySwitch.TOP_HEIGHT + KeySwitch.STEM_HEIGHT + Keycap.THICKNESS) }
         .forEach { hullPoints(it.points) }

      // ----

      val caseTopPlane = alphanumericTopPlane(case.alphanumericPlate, 0.mm)
      val frontRotaryEncoder = frontRotaryEncoder(case.alphanumericPlate)
         .translate(caseTopPlane.normalVector, 1.mm)

      rotaryEncoderKnob(
         frontRotaryEncoder,
         Case.FRONT_ROTARY_ENCODER_KNOB_RADIUS,
         Case.FRONT_ROTARY_ENCODER_KNOB_HEIGHT,
         Case.FRONT_ROTARY_ENCODER_KNOB_HOLE_HEIGHT
      )

      // ----

      translate((-62).mm, (-108).mm, 0.mm) {
         cube(102.mm, 70.mm, 80.mm)
      }

      translate((-91).mm, 56.mm, 23.mm) {
         cylinder(14.mm, 30.mm, `$fa`)
      }

      translate((-5).mm, 106.mm, 73.mm) {
         rotate(y = 75.deg) {
            cylinder(14.mm, 12.mm, `$fa`)
         }
      }
      */
   }
}

private data class Config(
   val outputFile: File = File("out.scad")
)

private class ArgumentParseException(message: String) : Exception(message)

/**
 * @throws ArgumentParseException
 */
private fun parseArguments(args: Array<out String>): Config {
   fun String.isParameterName() = startsWith('-')

   /**
    * このIteratorの[次の値][Iterator.next]が[引数名][isParameterName]でない場合それを返却
    * 次の値が引数名の場合はnullを返却
    */
   fun ListIterator<String>.nextArgumentOrNull(): String? {
      if (!hasNext()) { return null }

      val next = next()

      if (next.isParameterName()) {
         previous()
         return null
      }

      return next
   }

   fun ListIterator<String>.nextArgumentOrThrow(message: () -> String): String {
      return nextArgumentOrNull() ?: throw ArgumentParseException(message())
   }

   val iterator = args.toList().listIterator()
   var config = Config()

   while (iterator.hasNext()) {
      when (val parameterName = iterator.next()) {
         "--output-file", "-o" -> {
            val fileName = iterator.nextArgumentOrThrow { "Specify the output file name." }
            config = config.copy(outputFile = File(fileName))
         }

         else -> {
            if (parameterName.isParameterName()) {
               throw ArgumentParseException("Invalid argument: $parameterName")
            }
         }
      }
   }

   return config
}
