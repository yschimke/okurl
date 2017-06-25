package com.baulsupp.oksocial.network;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.google.common.net.InetAddresses;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import okhttp3.Dns;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class GoogleDns implements Dns {
  private List<InetAddress> dnsHosts;
  private IPvMode mode;
  private Supplier<OkHttpClient> client;

  public GoogleDns(List<InetAddress> dnsHosts, IPvMode mode, Supplier<OkHttpClient> client) {
    this.dnsHosts = dnsHosts;
    this.mode = mode;
    this.client = client;
  }

  // TODO implement DnsMode internally
  @Override public List<InetAddress> lookup(String host) throws UnknownHostException {
    if (host.equals("dns.google.com")) {
      return dnsHosts;
    }

    try {
      // TODO map punycode here?
      HttpUrl url = HttpUrl.parse("https://dns.google.com/resolve?name=" + host + "&type=" + type(mode));
      Request request = new Request.Builder().url(url).header("Accept", "application/dns+json").build();
      Map<String, Object> result = AuthUtil.makeJsonMapRequest(client.get(), request);

      return responseToList(result);
    } catch (IOException e) {
      UnknownHostException unknownHostException =
          new UnknownHostException("failed to lookup " + host + " via dns.google.com");
      unknownHostException.initCause(e);
      throw unknownHostException;
    }
  }

  private String type(IPvMode mode) {
    // TODO support IPv6 preferred etc, e.g. two queries
    switch (mode) {
      case IPV6_ONLY:
        return "AAAA";
      default:
        return "A";
    }
  }

  private List<InetAddress> responseToList(Map<String, Object> result) throws UnknownHostException {
    if (!result.get("Status").equals(0)) {
      // TODO response codes
      throw new UnknownHostException("Status from dns.google.com: " + result.get("Status"));
    }

    List<Map<String, Object>> answer = (List<Map<String, Object>>) result.get("Answer");

    return answer.stream()
        .filter(a -> a.get("type").equals(1) || a.get("type").equals(28))
        .map(a -> InetAddresses.forString((String) a.get("data")))
        .collect(
            Collectors.toList());
  }

  public static GoogleDns fromResourceList() {
    throw new UnsupportedOperationException();
  }

  public static GoogleDns fromHosts(Supplier<OkHttpClient> clientSupplier, IPvMode mode, String... ips) {
    List<InetAddress> hosts = new ArrayList<>();

    for (String ip : ips) {
      hosts.add(InetAddresses.forString(ip));
    }

    return new GoogleDns(hosts, mode, clientSupplier);
  }
}
