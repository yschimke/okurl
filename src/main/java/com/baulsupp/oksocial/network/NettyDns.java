package com.baulsupp.oksocial.network;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.resolver.ResolvedAddressTypes;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsNameResolverBuilder;
import io.netty.resolver.dns.DnsServerAddresses;
import io.netty.util.concurrent.Future;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.Dns;

import static io.netty.channel.socket.InternetProtocolFamily.IPv4;
import static io.netty.channel.socket.InternetProtocolFamily.IPv6;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class NettyDns implements Dns {
  private static Logger logger = Logger.getLogger(NettyDns.class.getName());

  private final DnsNameResolver r;
  private final EventLoopGroup group;
  private final Iterable<InetSocketAddress> dnsServers;

  public NettyDns(EventLoopGroup group, ResolvedAddressTypes addressTypes,
      Iterable<InetSocketAddress> dnsServers) {
    this.group = group;
    this.dnsServers = dnsServers;
    DnsNameResolverBuilder builder = new DnsNameResolverBuilder(this.group.next())
        .channelType(NioDatagramChannel.class)
        .optResourceEnabled(false)
        .maxQueriesPerResolve(3)
        .recursionDesired(true);

    if (logger.isLoggable(Level.FINEST)) {
      builder.traceEnabled(true);
    }

    if (this.dnsServers != null) {
      builder.nameServerAddresses(DnsServerAddresses.sequential(this.dnsServers));
    }

    if (addressTypes != null) {
      builder.resolvedAddressTypes(addressTypes);
    }

    r = builder.build();
  }

  @Override public List<InetAddress> lookup(String hostname) throws UnknownHostException {
    Future<List<InetAddress>> f = r.resolveAll(hostname);

    try {
      List<InetAddress> addresses = f.get();

      logger.fine("Dns (" + hostname + "): " + addresses.stream()
          .map(Object::toString)
          .collect(joining(", ")));

      return addresses;
    } catch (InterruptedException e) {
      throw new UnknownHostException(e.toString());
    } catch (ExecutionException e) {
      throw ((UnknownHostException) new UnknownHostException(e.getCause().getMessage()).initCause(
          e.getCause()));
    }
  }

  public static Dns byName(IPvMode ipMode, EventLoopGroup eventLoopGroup, String dnsServers) {
    ResolvedAddressTypes types = getInternetProtocolFamilies(ipMode);

    return new NettyDns(eventLoopGroup, types, getDnsServers(dnsServers));
  }

  private static List<InetSocketAddress> getDnsServers(String dnsServers) {
    if (dnsServers == null) {
      return DnsServerAddresses.defaultAddressList();
    }

    if (dnsServers.equals("google")) {
      return Arrays.asList(new InetSocketAddress("8.8.8.8", 53),
          new InetSocketAddress("8.8.4.4", 53));
    }

    return stream(dnsServers.split(",")).map(s -> new InetSocketAddress(s, 53)).collect(toList());
  }

  private static ResolvedAddressTypes getInternetProtocolFamilies(IPvMode ipMode) {
    switch (ipMode) {
      case IPV6_FIRST:
        return ResolvedAddressTypes.IPV6_PREFERRED;
      case IPV4_FIRST:
        return ResolvedAddressTypes.IPV4_PREFERRED;
      case IPV6_ONLY:
        return ResolvedAddressTypes.IPV6_ONLY;
      case IPV4_ONLY:
        return ResolvedAddressTypes.IPV4_ONLY;
      default:
        return null;
    }
  }
}
