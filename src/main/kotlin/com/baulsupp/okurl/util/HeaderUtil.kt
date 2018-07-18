package com.baulsupp.okurl.util

import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.okurl.util.FileUtil.expectedFile
import java.io.File
import java.io.IOException

// TODO handle duplicate header keys
object HeaderUtil {
  fun headerMap(headers: List<String>?): Map<String, String> {
    val headerMap = mutableMapOf<String, String>()

    headers?.forEach {
      if (it.startsWith("@")) {
        headerMap.putAll(headerFileMap(it))
      } else {
        val parts = it.split(":".toRegex(), 2).toTypedArray()
        // TODO: consider better strategy than simple trim
        val name = parts[0].trim { it <= ' ' }
        val value = stringValue(parts[1].trim { it <= ' ' })
        headerMap[name] = value
      }
    }

    return headerMap.toMap()
  }

  private fun headerFileMap(input: String): Map<out String, String> {
    return try {
      headerMap(inputFile(input).readLines())
    } catch (ioe: IOException) {
      throw UsageException("failed to read header file", ioe)
    }
  }

  fun stringValue(source: String): String {
    return if (source.startsWith("@")) {
      try {
        inputFile(source).readText()
      } catch (e: IOException) {
        throw UsageException(e.toString())
      }
    } else {
      source
    }
  }

  fun inputFile(path: String): File {
    return expectedFile(path.substring(1))
  }
}
