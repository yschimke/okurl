package com.baulsupp.oksocial.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.function.Consumer;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ListeningSSLSocketFactory extends SSLSocketFactory {
  private SSLSocketFactory delegate;
  private Consumer<SSLSocket> newListener;
  private HandshakeCompletedListener handshakeListener;

  public ListeningSSLSocketFactory(SSLSocketFactory delegate,
      Consumer<SSLSocket> newListener, HandshakeCompletedListener handshakeListener) {
    this.delegate = delegate;
    this.newListener = newListener;
    this.handshakeListener = handshakeListener;
  }

  @Override public String[] getDefaultCipherSuites() {
    return delegate.getDefaultCipherSuites();
  }

  @Override public String[] getSupportedCipherSuites() {
    return delegate.getSupportedCipherSuites();
  }

  @Override public Socket createSocket(Socket socket, String s, int i, boolean b)
      throws IOException {
    return enableLogging(delegate.createSocket(socket, s, i, b));
  }

  @Override public Socket createSocket(Socket socket, InputStream inputStream, boolean b)
      throws IOException {
    return enableLogging(delegate.createSocket(socket, inputStream, b));
  }

  @Override public Socket createSocket() throws IOException {
    return enableLogging(delegate.createSocket());
  }

  @Override public Socket createSocket(String s, int i) throws IOException {
    return enableLogging(delegate.createSocket(s, i));
  }

  @Override public Socket createSocket(String s, int i, InetAddress inetAddress, int i1)
      throws IOException {
    return enableLogging(delegate.createSocket(s, i, inetAddress, i1));
  }

  @Override public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
    return enableLogging(delegate.createSocket(inetAddress, i));
  }

  @Override
  public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1)
      throws IOException {
    return enableLogging(delegate.createSocket(inetAddress, i, inetAddress1, i1));
  }

  private Socket enableLogging(Socket socket) {
    if (socket != null && (socket instanceof SSLSocket)) {
      newListener.accept((SSLSocket) socket);

      ((SSLSocket) socket).addHandshakeCompletedListener(handshakeListener);
    }
    return socket;
  }
}
