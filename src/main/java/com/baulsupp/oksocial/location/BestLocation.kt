package com.baulsupp.oksocial.location

import java.io.IOException
import java.util.Optional

class BestLocation : LocationSource {
    @Throws(IOException::class)
    override fun read(): Optional<Location> {
        val fls = FileLocationSource(FileLocationSource.FILE)

        var result = fls.read()

        if (!result.isPresent) {
            val coreLoc = CoreLocationCLI()
            result = coreLoc.read()
        }

        return result
    }
}
