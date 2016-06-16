package com.baulsupp.oksocial.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import javax.net.SocketFactory;

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

  @Override
  public Socket createSocket(String host, int port) throws IOException {
    return systemFactory.createSocket(host, port, localAddress, 0);
  }

  @Override
  public Socket createSocket(InetAddress address, int port) throws IOException {
    return systemFactory.createSocket(address, port, localAddress, 0);
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localAddr, int localPort)
      throws IOException {
    return systemFactory.createSocket(host, port, localAddr, localPort);
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddr, int localPort)
      throws IOException {
    return systemFactory.createSocket(address, port, localAddr, localPort);
  }

  public static SocketFactory byName(String networkInterface) throws SocketException {
    InetAddress localAddress;
    try {
      localAddress = InetAddress.getByName(networkInterface);
    } catch (UnknownHostException uhe) {
      localAddress =
          NetworkInterface.getByName(networkInterface).getInetAddresses().nextElement();
    }

    return new InterfaceSocketFactory(localAddress);
  }
}
