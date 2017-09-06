package com.baulsupp.oksocial.location

import java.io.IOException
import java.util.Optional

interface LocationSource {
    @Throws(IOException::class)
    fun read(): Optional<Location>
}
