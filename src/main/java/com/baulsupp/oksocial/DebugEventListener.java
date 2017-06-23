package com.baulsupp.oksocial;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Handshake;

public class DebugEventListener extends EventListener {
  private static Logger logger = Logger.getLogger(DebugEventListener.class.getName());

  private Map<Call, Timing> timings = new ConcurrentHashMap<>();

  @Override public void fetchStart(Call call) {
    Timing timing = getTiming(call);

    logger.info("Fetch start " + describe(call));
  }

  private Timing getTiming(Call call) {
    Timing timing = timings.get(call);

    if (timing == null) {
      timing = new Timing();
      timing.callStart = System.currentTimeMillis();
      timings.put(call, timing);
    }

    return timing;
  }

  private String describe(Call call) {
    return call.request().url().host() + "@" + System.identityHashCode(call);
  }

  private String elapsed(long start) {
    return (System.currentTimeMillis() - start) + "ms";
  }

  @Override public void dnsStart(Call call, String domainName) {
    getTiming(call).dnsStart = System.currentTimeMillis();
    logger.info("DNS Start " + describe(call) + " " + domainName);
  }

  @Override public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList,
      Throwable throwable) {
    logger.info("DNS End " + describe(call) + " " + domainName + " " + elapsed(getTiming(call).dnsStart));
  }

  @Override public void connectStart(Call call, InetAddress address, int port) {
    getTiming(call).connectStart = System.currentTimeMillis();
    logger.info("Connect Start " + describe(call) + address);
  }

  @Override public void secureConnectStart(Call call) {
    getTiming(call).connectStart = System.currentTimeMillis();
    logger.info("Secure Connect Start " + describe(call));
  }

  @Override public void secureConnectEnd(Call call, Handshake handshake, Throwable throwable) {
    logger.log(Level.INFO, "Secure Connect End " + describe(call) + " " + elapsed(getTiming(call).connectStart), throwable);
  }

  @Override public void connectEnd(Call call, InetAddress address, int port, String protocol,
      Throwable throwable) {
    logger.log(Level.INFO, "Connect End " + describe(call) + " " + elapsed(getTiming(call).connectStart), throwable);
  }

  @Override public void requestHeadersStart(Call call) {
    logger.info("Response Headers " + describe(call));
  }

  @Override public void requestHeadersEnd(Call call, Throwable throwable) {
    logger.log(Level.INFO, "Response Body end " + describe(call), throwable);
  }

  @Override public void requestBodyStart(Call call) {
    logger.info("Request Body " + describe(call));
  }

  @Override public void requestBodyEnd(Call call, Throwable throwable) {
    logger.log(Level.INFO, "Request Body end " + describe(call), throwable);
  }

  @Override public void responseHeadersStart(Call call) {
    logger.info("Response Headers " + describe(call));
  }

  @Override public void responseHeadersEnd(Call call, Throwable throwable) {
    logger.log(Level.INFO, "Response Headers End " + describe(call), throwable);
  }

  @Override public void responseBodyStart(Call call) {
    logger.info("Response Body " + describe(call));
  }

  @Override public void responseBodyEnd(Call call, Throwable throwable) {
    logger.log(Level.INFO, "Response Body end " + describe(call), throwable);
  }

  @Override public void fetchEnd(Call call, Throwable throwable) {
    logger.info("Fetch end " + describe(call) + " " + elapsed(getTiming(call).dnsStart));
  }

  private class Timing {
    public long callStart;
    public long dnsStart;
    public long connectStart;
  }
}
