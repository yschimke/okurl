package com.baulsupp.okurl.location

import com.baulsupp.okurl.util.FileUtil
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

import java.io.File

class FileLocationSource(private val file: File) : LocationSource {
  override suspend fun read(): Location? {
    if (file.isFile) {
      val mapper = ObjectMapper()
      mapper.registerModule(KotlinModule())

      val values = mapper.readValue<Map<String, Double>>(file, object : TypeReference<Map<String, Double>>() {})

      return Location(values["latitude"]!!, values["longitude"]!!)
    }

    return null
  }

  fun save(location: Location) {
    val mapper = ObjectMapper()
    mapper.registerModule(KotlinModule())

    val map = linkedMapOf<String, Double>()
    map["latitude"] = location.latitude
    map["longitude"] = location.longitude

    mapper.writeValue(file, map)
  }

  companion object {
    var FILE = File(FileUtil.okurlSettingsDir, "location.json")
  }
}
