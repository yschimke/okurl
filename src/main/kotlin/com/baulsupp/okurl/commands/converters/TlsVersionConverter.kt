package com.baulsupp.okurl.commands.converters

import okhttp3.TlsVersion
import picocli.CommandLine.ITypeConverter

object TlsVersionConverter : ITypeConverter<TlsVersion>, Iterable<String> {
  override fun convert(value: String): TlsVersion = TlsVersion.forJavaName(value)

  override fun iterator(): Iterator<String> = TlsVersion.values()
    .map { it.javaName }
    .iterator()
}
