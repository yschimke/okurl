package com.baulsupp.oksocial.location

import com.baulsupp.oksocial.okhttp.OkHttpResponseExtractor
import com.baulsupp.oksocial.output.ConsoleHandler
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.oksocial.output.isOSX
import com.baulsupp.oksocial.output.process.exec
import com.baulsupp.oksocial.output.stdErrLogging
import okhttp3.Response
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.logging.Level
import java.util.logging.Logger

const val LOCATION_APP = "/usr/local/bin/CoreLocationCLI"

/**
 * https://github.com/fulldecent/corelocationcli
 */
class CoreLocationCLI(val outputHandler: OutputHandler<Response>) : LocationSource {
  private val logger = Logger.getLogger(CoreLocationCLI::class.java.name)

  override suspend fun read(): Location? {
    if (isOSX) {
      if (!File(LOCATION_APP).exists()) {
        throw UsageException("Missing $LOCATION_APP")
      }

      return try {
        val process = exec(listOf(LOCATION_APP, "-format",
          "%latitude,%longitude", "-once", "yes")) {
          timeout(5, TimeUnit.SECONDS)
          readOutput(true)
          redirectError(stdErrLogging)
        }

        val line = process.outputString

        if (!process.success) {
          logger.log(Level.INFO, "failed to get location $line")
          return null
        }

        println("'$line'")
        println("'${line.trim()}'")

        val parts = line.trim { it <= ' ' }.split(
          ",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        Location(parts[0].toDouble(), parts[1].toDouble())
      } catch (e: TimeoutException) {
        logger.log(Level.FINE, "failed to get location", e)
        outputHandler.showError(
          "Timeout fetching location, consider populating ~/.oksocial-location.json")
        return null
      } catch (e: Exception) {
        logger.log(Level.WARNING, "failed to get location", e)
        null
      }
    } else {
      return null
    }
  }
}

fun main(args: Array<String>) {
  CoreLocationCLI(ConsoleHandler.instance(OkHttpResponseExtractor()))
}
