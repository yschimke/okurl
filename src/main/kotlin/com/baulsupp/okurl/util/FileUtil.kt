package com.baulsupp.okurl.util

import com.baulsupp.schoutput.UsageException
import java.io.File

object FileUtil {
  val okurlSettingsDir = File(System.getProperty("user.home"), ".okurl").also {
    it.mkdirs()
  }

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
