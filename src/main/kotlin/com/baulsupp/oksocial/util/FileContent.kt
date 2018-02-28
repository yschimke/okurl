package com.baulsupp.oksocial.util

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object FileContent {

  fun readParamBytes(param: String): ByteArray {
    if (param == "@-") {
      return IOUtils.toByteArray(System.`in`)
    }
    return if (param.startsWith("@")) {
      FileUtils.readFileToByteArray(File(param.substring(1)))
    } else {
      param.toByteArray(StandardCharsets.UTF_8)
    }
  }

  fun readParamString(param: String): String {
    if (param == "@-") {
      return IOUtils.toString(System.`in`, Charset.defaultCharset())
    }
    return if (param.startsWith("@")) {
      FileUtils.readFileToString(File(param.substring(1)), StandardCharsets.UTF_8)
    } else {
      param
    }
  }
}
