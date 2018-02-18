package com.baulsupp.oksocial.location

interface LocationSource {
  suspend fun read(): Location?
}
