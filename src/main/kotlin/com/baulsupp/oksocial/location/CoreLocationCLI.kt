package com.baulsupp.oksocial.location

import com.baulsupp.oksocial.output.util.PlatformUtil
import com.baulsupp.oksocial.output.util.UsageException
import java.io.File
import java.util.Optional
import java.util.concurrent.TimeUnit
import org.zeroturnaround.exec.ProcessExecutor

/**
 * https://github.com/fulldecent/corelocationcli
 */
class CoreLocationCLI : LocationSource {

    override fun read(): Optional<Location> {
        if (PlatformUtil.isOSX()) {
            if (!File(LOCATION_APP).exists()) {
                throw UsageException("Missing " + LOCATION_APP)
            }

            try {
                val line = ProcessExecutor().command(LOCATION_APP, "-format",
                        "%latitude,%longitude", "-once", "yes")
                        .readOutput(true).timeout(5, TimeUnit.SECONDS).execute().outputUTF8()

                val parts = line.trim { it <= ' ' }.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                return Location.latLong(java.lang.Double.parseDouble(parts[0]), java.lang.Double.parseDouble(parts[1]))
            } catch (e: Exception) {
                e.printStackTrace()
                return Optional.empty()
            }

        } else {
            return Optional.empty()
        }
    }

    companion object {
        val LOCATION_APP = "/usr/local/bin/CoreLocationCLI"
    }
}
