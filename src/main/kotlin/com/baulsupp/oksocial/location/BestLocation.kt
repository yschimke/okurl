package com.baulsupp.oksocial.location

import java.io.IOException

class BestLocation : LocationSource {
    @Throws(IOException::class)
    override fun read(): Location? = FileLocationSource(FileLocationSource.FILE).read() ?: CoreLocationCLI().read()
}
