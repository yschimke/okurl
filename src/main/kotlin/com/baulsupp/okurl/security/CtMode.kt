package com.baulsupp.okurl.security

import com.baulsupp.oksocial.output.UsageException

enum class CtMode {
  OFF,
  LOG,
  FAIL;

  companion object {
    @JvmStatic
    fun fromString(ctMode: String): CtMode =
      values().find { it.name.toLowerCase() == ctMode } ?: throw UsageException(
        "unknown ct mode '$ctMode'"
      )
  }
}
