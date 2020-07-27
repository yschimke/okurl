package com.baulsupp.okurl.commands.converters

import com.baulsupp.okurl.network.IPvMode
import picocli.CommandLine.ITypeConverter

class IPvModeConverter : ITypeConverter<IPvMode>, Iterable<String> {
  override fun convert(value: String): IPvMode = IPvMode.fromString(value)
  override fun iterator(): Iterator<String> = IPvMode.values()
    .map { it.code }
    .iterator()
}
