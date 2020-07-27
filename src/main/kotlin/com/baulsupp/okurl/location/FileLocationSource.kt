package com.baulsupp.okurl.location

import com.baulsupp.okurl.Main
import com.baulsupp.okurl.kotlin.mapAdapter
import com.baulsupp.okurl.util.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import okio.source

import java.io.File

class FileLocationSource(private val file: File) : LocationSource {
  val mapAdapter = Main.moshi.mapAdapter<Double>()

  override suspend fun read(): Location? {
    return withContext(Dispatchers.IO) {
      if (file.isFile) {
        val values = mapAdapter
          .fromJson(file.source().buffer())

        if (values != null) {
          Location(values.getValue("latitude"), values.getValue("longitude"))
        }
      }

      null
    }
  }

  suspend fun save(location: Location) {
    withContext(Dispatchers.IO) {
      val map = linkedMapOf<String, Double>()
      map["latitude"] = location.latitude
      map["longitude"] = location.longitude

      mapAdapter.toJson(
        file.sink()
          .buffer(), map
      )
    }
  }

  companion object {
    var FILE = File(FileUtil.okurlSettingsDir, "location.json")
  }
}
