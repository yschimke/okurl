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

  private Call call;
  private long callStart;
  private long dnsStart;
  private long connectStart;
  private long secureConnectStart;

  public DebugEventListener(Call call) {
    this.call = call;
  }

  @Override public void fetchStart(Call call) {
    callStart = System.currentTimeMillis();
    logger.info("Fetch start " + describe());
  }

  private String describe() {
    return call.request().url().host() + "@" + System.identityHashCode(call);
  }

  private String elapsed(long start) {
    return (System.currentTimeMillis() - start) + "ms";
  }

  @Override public void dnsStart(Call call, String domainName) {
    dnsStart = System.currentTimeMillis();
    logger.info("DNS Start " + describe() + " " + domainName);
  }

  @Override public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList,
      Throwable throwable) {
    logger.info("DNS End " + describe() + " " + domainName + " " + elapsed(dnsStart));
  }

  @Override public void connectStart(Call call, InetAddress address, int port) {
    connectStart = System.currentTimeMillis();
    logger.info("Connect Start " + describe() + address);
  }

  @Override public void secureConnectStart(Call call) {
    secureConnectStart = System.currentTimeMillis();
    logger.info("Secure Connect Start " + describe());
  }

  @Override public void secureConnectEnd(Call call, Handshake handshake, Throwable throwable) {
    logger.log(Level.INFO, "Secure Connect End " + describe() + " " + handshake + " " + elapsed(secureConnectStart), throwable);
  }

  @Override public void connectEnd(Call call, InetAddress address, int port, String protocol,
      Throwable throwable) {
    logger.log(Level.INFO, "Connect End " + describe() + " " + elapsed(connectStart), throwable);
  }

  @Override public void requestHeadersStart(Call call) {
    logger.info("Response Headers " + describe());
  }

  @Override public void requestHeadersEnd(Call call, Throwable throwable) {
    logger.log(Level.INFO, "Response Body end " + describe(), throwable);
  }

  @Override public void requestBodyStart(Call call) {
    logger.info("Request Body " + describe());
  }

  @Override public void requestBodyEnd(Call call, Throwable throwable) {
    logger.log(Level.INFO, "Request Body end " + describe(), throwable);
  }

  @Override public void responseHeadersStart(Call call) {
    logger.info("Response Headers " + describe());
  }

  @Override public void responseHeadersEnd(Call call, Throwable throwable) {
    logger.log(Level.INFO, "Response Headers End " + describe(), throwable);
  }

  @Override public void responseBodyStart(Call call) {
    logger.info("Response Body " + describe());
  }

  @Override public void responseBodyEnd(Call call, Throwable throwable) {
    logger.log(Level.INFO, "Response Body end " + describe(), throwable);
  }

  @Override public void fetchEnd(Call call, Throwable throwable) {
    logger.info("Fetch end " + describe() + " " + elapsed(dnsStart));
  }
}
