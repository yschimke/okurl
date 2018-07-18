package com.baulsupp.okurl.location

interface LocationSource {
  suspend fun read(): Location?
}
