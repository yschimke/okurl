package com.baulsupp.oksocial.util

import com.soywiz.korio.vfs.toVfs
import org.apache.commons.io.IOUtils
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object FileContent {

  suspend fun readParamBytes(param: String): ByteArray {
    if (param == "@-") {
      return IOUtils.toByteArray(System.`in`)
    }
    return if (param.startsWith("@")) {
      File(param.substring(1)).toVfs().readBytes()
    } else {
      param.toByteArray(StandardCharsets.UTF_8)
    }
  }

  suspend fun readParamString(param: String): String {
    if (param == "@-") {
      return IOUtils.toString(System.`in`, Charset.defaultCharset())
    }
    return if (param.startsWith("@")) {
      File(param.substring(1)).toVfs().readString()
    } else {
      param
    }
  }

}
