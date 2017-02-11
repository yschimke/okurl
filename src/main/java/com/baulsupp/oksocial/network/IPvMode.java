package com.baulsupp.oksocial.network;

public enum IPvMode {
  SYSTEM,
  IPV6_FIRST,
  IPV4_FIRST,
  IPV6_ONLY,
  IPV4_ONLY;

  public static IPvMode fromString(String ipMode) {
    switch (ipMode) {
      case "ipv6":
        return IPvMode.IPV6_FIRST;
      case "ipv4":
        return IPvMode.IPV4_FIRST;
      case "ipv6only":
        return IPvMode.IPV6_ONLY;
      case "ipv4only":
        return IPvMode.IPV4_ONLY;
      default:
        return IPvMode.SYSTEM;
    }
  }
}
