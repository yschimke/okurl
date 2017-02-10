package com.baulsupp.oksocial.network;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsNameResolverBuilder;
import io.netty.util.concurrent.Future;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.Dns;

import static io.netty.channel.socket.InternetProtocolFamily.IPv4;
import static io.netty.channel.socket.InternetProtocolFamily.IPv6;
import static java.util.stream.Collectors.joining;

public class NettyDns implements Dns {
  private static Logger logger = Logger.getLogger(NettyDns.class.getName());

  private final DnsNameResolver r;
  private final EventLoopGroup group;

  public NettyDns(EventLoopGroup group, Iterable<InternetProtocolFamily> addressTypes) {
    this.group = group;
    DnsNameResolverBuilder builder = new DnsNameResolverBuilder(this.group.next())
        .channelType(NioDatagramChannel.class)
        .optResourceEnabled(false)
        .maxQueriesPerResolve(3)
        .recursionDesired(true);

    if (logger.isLoggable(Level.FINEST)) {
      builder.traceEnabled(true);
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

  public static Dns byName(String ipMode, EventLoopGroup eventLoopGroup) {
    List<InternetProtocolFamily> types;

    switch (ipMode) {
      case "ipv6":
        types = Arrays.asList(IPv6, IPv4);
        break;
      case "ipv4":
        types = Arrays.asList(IPv4, IPv6);
        break;
      case "ipv6only":
        types = Arrays.asList(IPv6);
        break;
      case "ipv4only":
        types = Arrays.asList(IPv4);
        break;
      default:
        types = null;
        break;
    }

    return new NettyDns(eventLoopGroup, types);
  }
}
