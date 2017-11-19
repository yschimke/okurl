package com.baulsupp.oksocial.util

import com.google.common.collect.Lists
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.JdkLoggerFactory
import okhttp3.internal.http2.Http2
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

class LoggingUtil {
  companion object {
    private val activeLoggers = Lists.newArrayList<Logger>()

    fun configureLogging(debug: Boolean, showHttp2Frames: Boolean) {
      InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE)

      if (debug || showHttp2Frames) {
        LogManager.getLogManager().reset()
        val handler = ConsoleHandler()

        if (debug) {
          handler.level = Level.ALL
          handler.formatter = OneLineLogFormat()
          val activeLogger = getLogger("")
          activeLogger.addHandler(handler)
          activeLogger.level = Level.ALL

          getLogger("org.zeroturnaround.exec").level = Level.INFO
          getLogger("io.netty").level = Level.INFO
          getLogger("io.netty.resolver.dns").level = Level.FINE
        } else if (showHttp2Frames) {
          val activeLogger = getLogger(Http2::class.java.name)
          activeLogger.level = Level.FINE
          handler.level = Level.FINE
          handler.formatter = object : SimpleFormatter() {
            override fun format(record: LogRecord): String {
              return String.format("%s%n", record.message)
            }
          }
          activeLogger.addHandler(handler)
          getLogger("io.netty.resolver.dns.DnsServerAddresses").level = Level.SEVERE
        }
      } else {
        getLogger("io.netty.resolver.dns.DnsServerAddresses").level = Level.SEVERE
      }
    }

    fun getLogger(name: String): Logger {
      val logger = Logger.getLogger(name)
      activeLoggers.add(logger)
      return logger
    }
  }
}
