package com.baulsupp.oksocial.network;

import com.baulsupp.oksocial.output.util.UsageException;

public enum DnsMode {
  JAVA,
  NETTY,
  DNSGOOGLE;

  public static DnsMode fromString(String dnsMode) {
    switch (dnsMode) {
      case "java":
        return DnsMode.JAVA;
      case "netty":
        return DnsMode.NETTY;
      case "dnsgoogle":
        return DnsMode.DNSGOOGLE;
      default:
        throw new UsageException("unknown dns mode '" + dnsMode + "'");
    }
  }
}
