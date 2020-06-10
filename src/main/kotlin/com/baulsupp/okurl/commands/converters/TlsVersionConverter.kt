package com.baulsupp.okurl.commands.converters

import okhttp3.TlsVersion
import picocli.CommandLine.ITypeConverter

object TlsVersionConverter : ITypeConverter<TlsVersion> {
  override fun convert(value: String): TlsVersion = TlsVersion.forJavaName(value)
}
