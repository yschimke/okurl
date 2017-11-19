package com.baulsupp.oksocial.authenticator

import org.eclipse.jetty.util.log.Logger

internal class NullLogger : Logger {
  override fun getName(): String {
    return "nolog"
  }

  override fun warn(s: String, vararg objects: Any) {

  }

  override fun warn(throwable: Throwable) {

  }

  override fun warn(s: String, throwable: Throwable) {

  }

  override fun info(s: String, vararg objects: Any) {

  }

  override fun info(throwable: Throwable) {

  }

  override fun info(s: String, throwable: Throwable) {

  }

  override fun isDebugEnabled(): Boolean {
    return false
  }

  override fun setDebugEnabled(b: Boolean) {

  }

  override fun debug(s: String, vararg objects: Any) {

  }

  override fun debug(s: String, l: Long) {

  }

  override fun debug(throwable: Throwable) {

  }

  override fun debug(s: String, throwable: Throwable) {

  }

  override fun getLogger(s: String): Logger {
    return this
  }

  override fun ignore(throwable: Throwable) {

  }
}
