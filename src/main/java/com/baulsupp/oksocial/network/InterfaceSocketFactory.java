package com.baulsupp.oksocial.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Optional;
import javax.net.SocketFactory;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class InterfaceSocketFactory extends SocketFactory {
  private InetAddress localAddress;
  private SocketFactory systemFactory = SocketFactory.getDefault();

  public InterfaceSocketFactory(InetAddress localAddress) {
    this.localAddress = localAddress;
  }

  @Override public Socket createSocket() throws IOException {
    Socket s = systemFactory.createSocket();
    s.bind(new InetSocketAddress(localAddress, 0));
    return s;
  }

  @Override public Socket createSocket(String host, int port) throws IOException {
    return systemFactory.createSocket(host, port, localAddress, 0);
  }

  @Override public Socket createSocket(InetAddress address, int port) throws IOException {
    return systemFactory.createSocket(address, port, localAddress, 0);
  }

  @Override public Socket createSocket(String host, int port, InetAddress localAddr, int localPort)
      throws IOException {
    return systemFactory.createSocket(host, port, localAddr, localPort);
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddr, int localPort)
      throws IOException {
    return systemFactory.createSocket(address, port, localAddr, localPort);
  }

  public static Optional<SocketFactory> byName(String ipOrInterface) throws SocketException {
    InetAddress localAddress;
    try {
      // example 192.168.0.51
      localAddress = InetAddress.getByName(ipOrInterface);
    } catch (UnknownHostException uhe) {
      // example en0
      NetworkInterface networkInterface = NetworkInterface.getByName(ipOrInterface);

      if (networkInterface == null) {
        return empty();
      }

      localAddress = networkInterface.getInetAddresses().nextElement();
    }

    return of(new InterfaceSocketFactory(localAddress));
  }
}
