package com.baulsupp.oksocial.util

import com.baulsupp.oksocial.output.UsageException
import java.io.File

object FileUtil {
  val oksocialSettingsDir = File(System.getenv("HOME"), ".oksocial")

  fun expectedFile(name: String): File {
    val file = File(normalize(name))

    if (!file.isFile) {
      throw UsageException("file not found: $file")
    }
    return file
  }

  private fun normalize(path: String): String {
    return if (path.startsWith("~/")) {
      System.getenv("HOME") + "/" + path.substring(2)
    } else {
      path
    }
  }
}
