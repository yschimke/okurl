package com.baulsupp.okurl.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.time.Instant

object Rfc3339InstantJsonAdapter : JsonAdapter<Instant>() {
  override fun fromJson(reader: JsonReader): Instant = Instant.parse(reader.nextString())

  override fun toJson(writer: JsonWriter, value: Instant?) {
    writer.value(value?.toString())
  }
}
