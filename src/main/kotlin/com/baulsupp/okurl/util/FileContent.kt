package com.baulsupp.okurl.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.apache.commons.io.IOUtils
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object FileContent {
  suspend fun readParamBytes(param: String): ByteArray = coroutineScope {
    when {
      param == "@-" -> IOUtils.toByteArray(System.`in`)
      param.startsWith("@") -> async(Dispatchers.IO) { File(param.substring(1)).readBytes() }.await()
      else -> param.toByteArray(StandardCharsets.UTF_8)
    }
  }

  suspend fun readParamString(param: String): String = coroutineScope {
    when {
      param == "@-" -> IOUtils.toString(System.`in`, Charset.defaultCharset())
      param.startsWith("@") -> async(Dispatchers.IO) { File(param.substring(1)).readText() }.await()
      else -> param
    }
  }
}
