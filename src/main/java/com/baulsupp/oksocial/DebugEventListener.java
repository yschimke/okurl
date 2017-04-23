package com.baulsupp.oksocial;

import java.net.InetAddress;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Handshake;

public class DebugEventListener extends EventListener {
  private static Logger logger = Logger.getLogger(DebugEventListener.class.getName());

  @Override public void fetchStart(Call call) {
    logger.info("Fetch start " + call);
  }

  @Override public void dnsStart(Call call, String domainName) {
    logger.info("DNS Start " + call + " " + domainName);
  }

  @Override public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList,
      Throwable throwable) {
    logger.info("DNS End " + call + " " + domainName);
  }

  @Override public void connectStart(Call call, InetAddress address, int port) {
    logger.info("Connect Start " + call + address);
  }

  @Override public void secureConnectStart(Call call) {
    logger.info("Secure Connect Start " + call);
  }

  @Override public void secureConnectEnd(Call call, Handshake handshake, Throwable throwable) {
    logger.log(Level.INFO, "Secure Connect End " + call, throwable);
  }

  @Override public void connectEnd(Call call, InetAddress address, int port, String protocol,
      Throwable throwable) {
    logger.log(Level.INFO, "Connect End " + call, throwable);
  }

  @Override public void requestHeadersStart(Call call) {
    logger.info("Response Headers " + call);
  }

  @Override public void requestHeadersEnd(Call call, Throwable throwable) {
    logger.log(Level.INFO, "Response Body end " + call, throwable);
  }

  @Override public void requestBodyStart(Call call) {
    logger.info("Request Body " + call);
  }

  @Override public void requestBodyEnd(Call call, Throwable throwable) {
    logger.log(Level.INFO, "Request Body end " + call, throwable);
  }

  @Override public void responseHeadersStart(Call call) {
    logger.info("Response Headers " + call);
  }

  @Override public void responseHeadersEnd(Call call, Throwable throwable) {
    logger.log(Level.INFO, "Response Headers End " + call, throwable);
  }

  @Override public void responseBodyStart(Call call) {
    logger.info("Response Body " + call);
  }

  @Override public void responseBodyEnd(Call call, Throwable throwable) {
    logger.log(Level.INFO, "Response Body end " + call, throwable);
  }

  @Override public void fetchEnd(Call call, Throwable throwable) {
    // TODO duration
    logger.info("Fetch end " + call);
  }
}
