package com.baulsupp.oksocial.location

import java.io.IOException

interface LocationSource {
    @Throws(IOException::class)
    fun read(): Location?
}
