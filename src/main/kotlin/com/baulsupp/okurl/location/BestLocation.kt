package com.baulsupp.okurl.location

import com.baulsupp.schoutput.handler.OutputHandler
import okhttp3.Response

class BestLocation(val outputHandler: OutputHandler<Response>) : LocationSource {
  override suspend fun read(): Location? = FileLocationSource(FileLocationSource.FILE).read()
    ?: CoreLocationCLI(outputHandler).read()
}
