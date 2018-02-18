package com.baulsupp.oksocial.location

import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.Response

class BestLocation(val outputHandler: OutputHandler<Response>) : LocationSource {
  suspend override fun read(): Location? = FileLocationSource(FileLocationSource.FILE).read()
    ?: CoreLocationCLI(outputHandler).read()
}
