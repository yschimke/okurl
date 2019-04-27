package com.baulsupp.okurl.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object FileContent {
  suspend fun readParamBytes(param: String): ByteArray = coroutineScope {
    when {
      param == "@-" -> IOUtils.toByteArray(System.`in`)
      param.startsWith("@") -> withContext(Dispatchers.IO) {
        File(param.substring(1)).readBytes()
      }
      else -> param.toByteArray(StandardCharsets.UTF_8)
    }
  }

  suspend fun readParamString(param: String): String = coroutineScope {
    when {
      param == "@-" -> IOUtils.toString(System.`in`, Charset.defaultCharset())
      param.startsWith("@") -> withContext(Dispatchers.IO) {
        File(param.substring(1)).readText()
      }
      else -> param
    }
  }
}
