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
      val case = Case()
      case(case)

      (
            case.alphanumericPlate.columns
               .flatMap { it.keySwitches }
               .map { it.plate(AlphanumericPlate.KEY_PLATE_SIZE) }
            + case.thumbHomeKey.plate(ThumbPlate.KEY_PLATE_SIZE)
            + case.thumbPlate.keySwitches
               .map { it.plate(ThumbPlate.KEY_PLATE_SIZE) }
            + case.frontRotaryEncoderKey.switch
               .plate(ThumbPlate.KEY_PLATE_SIZE)
         )
         .map { it.translate(it.topVector, KeySwitch.TOP_HEIGHT + KeySwitch.STEM_HEIGHT + Keycap.THICKNESS) }
         .forEach { hullPoints(it.points) }

      // ----

      frontRotaryEncoderKnob(case.frontRotaryEncoderKnob)
      backRotaryEncoderKnob(case.backRotaryEncoderKnob)

      backRotaryEncoderMediationGear(case.backRotaryEncoderMediationGear)
      backRotaryEncoderGear(case.backRotaryEncoderGear)
      rotaryEncoderMountHole(case.backRotaryEncoderGear.rotaryEncoder, 1.6.mm)

      // ----

      translate((-62).mm, (-108).mm, 0.mm) {
         cube(102.mm, 70.mm, 80.mm)
      }

      leftOuterRotaryEncoderKnob(case.leftOuterRotaryEncoderKnob)
      leftInnerRotaryEncoderKnob(case.leftInnerRotaryEncoderKnob)

      leftOuterRotaryEncoderGear(case.leftOuterRotaryEncoderKnob.gear)

      locale(case.leftOuterRotaryEncoderKnob.referencePoint) {
         translate(z = LeftOuterRotaryEncoderKnob.HEIGHT - 3.mm) {
            circularProtuberance(
               LeftOuterRotaryEncoderKnob.RADIUS
                     + LeftOuterRotaryEncoderKnob.PROTUBERANCE_RADIUS
                     + 0.1.mm
                     + LeftOuterRotaryEncoderKnob.PROTUBERANCE_RADIUS,
               LeftOuterRotaryEncoderKnob.PROTUBERANCE_RADIUS
            )
         }

         translate(z = -LeftOuterRotaryEncoderKnob.PROTUBERANCE_RADIUS - 0.1.mm) {
            circularProtuberance(
               LeftOuterRotaryEncoderKnob.RADIUS
                     - LeftOuterRotaryEncoderKnob.PROTUBERANCE_RADIUS,
               LeftOuterRotaryEncoderKnob.PROTUBERANCE_RADIUS
            )
         }
      }

      rotaryEncoderMountHole(
         case.leftOuterRotaryEncoderKnob.gear.rotaryEncoder,
         1.6.mm
      )

      rotaryEncoderMountHole(
         case.leftInnerRotaryEncoderKnob.rotaryEncoder,
         1.6.mm
      )
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