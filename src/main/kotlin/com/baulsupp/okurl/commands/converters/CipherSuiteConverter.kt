package com.baulsupp.okurl.commands.converters

import okhttp3.CipherSuite
import picocli.CommandLine.ITypeConverter
import kotlin.reflect.full.memberProperties

class CipherSuiteConverter : ITypeConverter<CipherSuite>, Iterable<String> {
  override fun convert(value: String): CipherSuite = CipherSuite.forJavaName(value)

  override fun iterator(): Iterator<String> =
    CipherSuite.Companion::class.memberProperties.filter { it.name.startsWith("TLS_") }
      .map { it.name }
      .iterator()
}
