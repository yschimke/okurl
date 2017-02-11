package com.baulsupp.oksocial.network;

import com.baulsupp.oksocial.util.UsageException;

public enum DnsMode {
  JAVA,
  NETTY;

  public static DnsMode fromString(String dnsMode) {
    switch (dnsMode) {
      case "java":
        return DnsMode.JAVA;
      case "netty":
        return DnsMode.NETTY;
      default:
        throw new UsageException("unknown dns mode '" + dnsMode + "'");
    }
  }
}
