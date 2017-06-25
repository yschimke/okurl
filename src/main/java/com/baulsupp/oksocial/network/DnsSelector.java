package com.baulsupp.oksocial.network;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import okhttp3.Dns;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class DnsSelector implements Dns {
  private static Logger logger = Logger.getLogger(DnsSelector.class.getName());

  private IPvMode mode;
  private Dns delegate;

  public DnsSelector(IPvMode mode, Dns delegate) {
    this.mode = mode;
    this.delegate = delegate;
  }

  @Override public List<InetAddress> lookup(String hostname) throws UnknownHostException {
    List<InetAddress> addresses = delegate.lookup(hostname);

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

    logger.fine("Dns (" + hostname + "): " + addresses.stream()
        .map(Object::toString)
        .collect(joining(", ")));

    return addresses;
  }
}
