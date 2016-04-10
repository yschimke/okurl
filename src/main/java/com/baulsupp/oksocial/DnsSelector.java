package com.baulsupp.oksocial;

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

  @Override public List<InetAddress> lookup(String hostname) throws UnknownHostException {
    List<InetAddress> addresses = overrides.get(hostname.toLowerCase());

    if (addresses != null) {
      return addresses;
    }

    addresses = Dns.SYSTEM.lookup(hostname);

    switch (mode) {
      case IPV6_FIRST:
        addresses.sort(Comparator.comparing(Inet4Address.class::isInstance));
        return addresses;
      case IPV4_FIRST:
        addresses.sort(Comparator.comparing(Inet4Address.class::isInstance).reversed());
        return addresses;
      case IPV6_ONLY:
        return addresses.stream().filter(Inet4Address.class::isInstance).collect(toList());
      case IPV4_ONLY:
        return addresses.stream().filter(Inet6Address.class::isInstance).collect(toList());
    }

    return addresses;
  }

  public void addOverride(String hostname, InetAddress address) {
    overrides.put(hostname.toLowerCase(), Lists.newArrayList(address));
  }
}
