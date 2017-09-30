package com.baulsupp.oksocial.location

import com.baulsupp.oksocial.output.util.PlatformUtil
import com.baulsupp.oksocial.output.util.UsageException
import org.zeroturnaround.exec.ProcessExecutor
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * https://github.com/fulldecent/corelocationcli
 */
class CoreLocationCLI : LocationSource {

    override fun read(): Location? {
        if (PlatformUtil.isOSX) {
            if (!File(LOCATION_APP).exists()) {
                throw UsageException("Missing " + LOCATION_APP)
            }

            return try {
                val line = ProcessExecutor().command(LOCATION_APP, "-format",
                        "%latitude,%longitude", "-once", "yes")
                        .readOutput(true).timeout(5, TimeUnit.SECONDS).execute().outputUTF8()

                val parts = line.trim { it <= ' ' }.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                Location(parts[0].toDouble(), parts[1].toDouble())
            } catch (e: Exception) {
                e.printStackTrace()
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
