package com.baulsupp.oksocial.network;

import com.google.common.collect.Maps;
import ee.schimke.oksocial.output.util.UsageException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import okhttp3.Dns;

public class DnsOverride implements Dns {
  private static Logger logger = Logger.getLogger(DnsOverride.class.getName());

  private final Dns dns;
  private Map<String, String> overrides = Maps.newHashMap();

  public DnsOverride(Dns dns) {
    this.dns = dns;
  }

  private void put(String host, String target) {
    overrides.put(host, target);
  }

  @Override public List<InetAddress> lookup(String hostname) throws UnknownHostException {
    String override = overrides.get(hostname);

    if (override != null) {
      logger.fine("Using Dns Override (" + hostname + "): " + override);
      return Collections.singletonList(InetAddress.getByName(override));
    }

    return dns.lookup(hostname);
  }

  public static DnsOverride build(Dns dns, List<String> resolveStrings) {
    DnsOverride dnsOverride = new DnsOverride(dns);

    for (String resolveString : resolveStrings) {
      String[] parts = resolveString.split(":");

      if (parts.length != 2) {
        throw new UsageException("Invalid resolve string '" + resolveString + "'");
      }

      dnsOverride.put(parts[0], parts[1]);
    }

    return dnsOverride;
  }
}
