package com.baulsupp.okurl.util

import java.net.InetSocketAddress

class InetAddressParam(hostAndPort: String) {
  var address: InetSocketAddress

  init {
    val parts = hostAndPort.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    this.address = InetSocketAddress.createUnresolved(parts[0], Integer.parseInt(parts[1]))
  }
}
