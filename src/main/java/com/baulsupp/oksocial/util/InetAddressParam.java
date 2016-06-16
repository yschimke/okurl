package com.baulsupp.oksocial.util;

import java.net.InetSocketAddress;

public class InetAddressParam {
  public InetSocketAddress address;

  public InetAddressParam(String hostAndPort) {
    String[] parts = hostAndPort.split(":");
    this.address = InetSocketAddress.createUnresolved(parts[0], Integer.parseInt(parts[1]));
  }
}
