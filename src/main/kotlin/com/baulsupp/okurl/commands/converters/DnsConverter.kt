package com.baulsupp.okurl.commands.converters

import com.baulsupp.okurl.network.DnsMode
import picocli.CommandLine.ITypeConverter

object DnsConverter : ITypeConverter<DnsMode>, Iterable<String> {
  override fun convert(value: String): DnsMode = DnsMode.fromString(value)

  override fun iterator(): Iterator<String> = DnsMode.values()
    .map { it.name.toLowerCase() }
    .iterator()
}
