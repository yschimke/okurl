package com.baulsupp.oksocial.location

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.util.PlatformUtil
import com.baulsupp.oksocial.output.util.UsageException
import okhttp3.Response
import org.zeroturnaround.exec.ProcessExecutor
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * https://github.com/fulldecent/corelocationcli
 */
class CoreLocationCLI(val outputHandler: OutputHandler<Response>) : LocationSource {
  private val logger = Logger.getLogger(CoreLocationCLI::class.java.name)

  override fun read(): Location? {
    if (PlatformUtil.isOSX) {
      if (!File(LOCATION_APP).exists()) {
        throw UsageException("Missing " + LOCATION_APP)
      }

      return try {
        val process = ProcessExecutor().command(LOCATION_APP, "-format",
                "%latitude,%longitude", "-once", "yes")
                .readOutput(true).timeout(5, TimeUnit.SECONDS).execute()
        val line = process.outputUTF8()

        if (process.exitValue != 0) {
          logger.log(Level.INFO, "failed to get location $line")
          return null
        }

        val parts = line.trim { it <= ' ' }.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        Location(parts[0].toDouble(), parts[1].toDouble())
      } catch (e: TimeoutException) {
        logger.log(Level.FINE, "failed to get location", e)
        outputHandler.showError("Timeout fetching location, consider populating ~/.oksocial-location.json")
        return null
      } catch (e: Exception) {
        logger.log(Level.WARNING, "failed to get location", e)
        null
      }
    } else {
      return null
    }
  }

  companion object {
    val LOCATION_APP = "/usr/local/bin/CoreLocationCLI"
  }
}
