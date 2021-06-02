package com.wcaokaze.ninja60

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

      val columns = (0.mm .. (-20).mm step (-5).mm) .map { Column(Point3d.ORIGIN, 60.mm, it) }

      for (c in columns) {
         for (p in c.keyPlates) {
            hullPoints(p.points)
         }
      }
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
