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
      prepareSharedScads()

      /*
      prepareBottomPlateModule()
      prepareMiddlePlateModule()

      translate(x = 19.05.mm * 2) { rotate(y = Angle.PI) { bottomPlate() } }
      translate(z = 1.cm) { middlePlate() }
      translate(z = 2.cm) { topPlate() }

      leftKeys()
      */

      translate((-91).mm, 56.mm, (-30).mm) {
         encoderKnob(30.mm)
      }

      translate(7.mm, 29.mm, 15.mm) {
         rotate(y = (-14).deg) { encoderKnob(18.mm) }
      }

      translate((-5).mm, 106.mm, 20.mm) {
         rotate(y = 75.deg) { encoderKnob(12.mm) }
      }

      alphanumericPlate(
         AlphanumericPlate()
            .rotate(Line3d.Y_AXIS, (-15).deg)
            .translate(x = 0.mm, y = 69.mm, z = 32.mm)
      )

      thumbPlate(
         ThumbPlate()
            .rotate(Line3d.Y_AXIS, 69.deg)
            .rotate(Line3d.X_AXIS, (-7).deg)
            .rotate(Line3d.Z_AXIS, (-8).deg)
            .translate(x = 66.mm, y = 0.mm, z = 0.mm)
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
