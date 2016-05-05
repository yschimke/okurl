package com.baulsupp.oksocial.dns;

import com.baulsupp.oksocial.UsageException;
import com.google.common.collect.Maps;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import okhttp3.Dns;

public class DnsOverride implements Dns {
  private final Dns dns;
  private Map<String, String> overrides = Maps.newHashMap();

  public DnsOverride(Dns dns) {
    this.dns = dns;
  }

  private void put(String host, String target) {
    overrides.put(host, target);
  }

  @Override public List<InetAddress> lookup(String s) throws UnknownHostException {
    String override = overrides.get(s);

    if (override != null) {
      return Arrays.asList(InetAddress.getByName(override));
    }

    return dns.lookup(s);
  }

  public static DnsOverride build(Dns dns, String resolveString) {
    DnsOverride dnsOverride = new DnsOverride(dns);

    String[] parts = resolveString.split(":");

    if (parts.length != 2) {
      throw new UsageException("Invalid resolve string '" + resolveString + "'");
    }

    dnsOverride.put(parts[0], parts[1]);

    return dnsOverride;
  }
}
