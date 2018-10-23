package com.baulsupp.okurl.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
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
      GlobalScope.async(Dispatchers.IO) { File(param.substring(1)).readBytes() }.await()
    } else {
      param.toByteArray(StandardCharsets.UTF_8)
    }
  }

  suspend fun readParamString(param: String): String {
    if (param == "@-") {
      return IOUtils.toString(System.`in`, Charset.defaultCharset())
    }
    return if (param.startsWith("@")) {
      GlobalScope.async(Dispatchers.IO) { File(param.substring(1)).readText() }.await()
    } else {
      param
    }
  }
}
