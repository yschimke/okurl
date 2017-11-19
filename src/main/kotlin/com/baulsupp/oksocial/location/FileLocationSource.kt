package com.baulsupp.oksocial.location

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.common.collect.Maps
import java.io.File
import java.io.IOException

class FileLocationSource(private val file: File) : LocationSource {

  @Throws(IOException::class)
  override fun read(): Location? {
    if (file.isFile) {
      val mapper = ObjectMapper()
      mapper.registerModule(KotlinModule())

      val values = mapper.readValue<Map<String, Double>>(file, object : TypeReference<Map<String, Double>>() {})

      return Location(values["latitude"]!!, values["longitude"]!!)
    }

    return null
  }

  @Throws(IOException::class)
  fun save(location: Location) {
    val mapper = ObjectMapper()
    mapper.registerModule(KotlinModule())

    val map = Maps.newLinkedHashMap<String, Double>()
    map.put("latitude", location.latitude)
    map.put("longitude", location.longitude)

    mapper.writeValue(file, map)
  }

  companion object {
    var FILE = File(System.getenv("HOME"), ".oksocial-location.json")
  }
}
