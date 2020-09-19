package com.baulsupp.okurl.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import java.time.Instant

class Rfc3339InstantJsonAdapter {
  @FromJson
  fun fromJson(s: String): Instant = Instant.parse(s)

  @ToJson
  fun toJson(value: Instant) = value.toString()
}
