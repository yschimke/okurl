package com.baulsupp.oksocial.brave;

import com.github.kristofa.brave.Brave;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import okhttp3.Dns;

public class BraveDns implements Dns {
  private Dns delegate;
  private Brave brave;

  public BraveDns(Dns delegate, Brave brave) {
    this.delegate = delegate;
    this.brave = brave;
  }

  @Override public List<InetAddress> lookup(String s) throws UnknownHostException {
    brave.clientTracer().startNewSpan("dns");

    try {
      brave.clientTracer().setClientSent();

      List<InetAddress> result = delegate.lookup(s);

      brave.clientTracer().submitBinaryAnnotation("dns", result.toString());

      return result;
    } finally {
      brave.clientTracer().setClientReceived();
    }
  }
}
