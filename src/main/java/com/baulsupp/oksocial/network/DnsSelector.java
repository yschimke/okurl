package com.baulsupp.oksocial.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import okhttp3.Dns;

import static java.util.stream.Collectors.toList;

public class DnsSelector implements Dns {

  public enum Mode {
    SYSTEM,
    IPV6_FIRST,
    IPV4_FIRST,
    IPV6_ONLY,
    IPV4_ONLY
  }

  private Map<String, List<InetAddress>> overrides = Maps.newHashMap();

  private Mode mode;

  public DnsSelector(Mode mode) {
    this.mode = mode;
  }

  public static Dns byName(String ipMode) {
    Mode selectedMode;
    switch (ipMode) {
      case "ipv6":
        selectedMode = Mode.IPV6_FIRST;
        break;
      case "ipv4":
        selectedMode = Mode.IPV4_FIRST;
        break;
      case "ipv6only":
        selectedMode = Mode.IPV6_ONLY;
        break;
      case "ipv4only":
        selectedMode = Mode.IPV4_ONLY;
        break;
      default:
        selectedMode = Mode.SYSTEM;
        break;
    }

    return new DnsSelector(selectedMode);
  }

  @Override
  public List<InetAddress> lookup(String hostname) throws UnknownHostException {
    List<InetAddress> addresses = overrides.get(hostname.toLowerCase());

    if (addresses != null) {
      return addresses;
    }

    addresses = Dns.SYSTEM.lookup(hostname);

    switch (mode) {
      case IPV6_FIRST:
        addresses.sort(Comparator.comparing(Inet4Address.class::isInstance));
        break;
      case IPV4_FIRST:
        addresses.sort(Comparator.comparing(Inet4Address.class::isInstance).reversed());
        break;
      case IPV6_ONLY:
        addresses = addresses.stream().filter(Inet6Address.class::isInstance).collect(toList());
        break;
      case IPV4_ONLY:
        addresses = addresses.stream().filter(Inet4Address.class::isInstance).collect(toList());
        break;
    }

    return addresses;
  }

  public void addOverride(String hostname, InetAddress address) {
    overrides.put(hostname.toLowerCase(), Lists.newArrayList(address));
  }
}
