package com.baulsupp.okurl.util

import java.util.logging.LogRecord
import java.util.logging.SimpleFormatter

object MessageFormatter : SimpleFormatter() {
  override fun format(record: LogRecord): String {
    return String.format("%s%n", record.message)
  }
}
