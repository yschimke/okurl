package com.baulsupp.oksocial;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class InetAddress {
  public SocketAddress address;

  public InetAddress(String hostAndPort) {
    String[] parts = hostAndPort.split(":");
    this.address = InetSocketAddress.createUnresolved(parts[0], Integer.parseInt(parts[1]));
  }
}
