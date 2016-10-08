package com.baulsupp.oksocial.brave;

import java.net.InetSocketAddress;

public class BraveUtil {

  public static String getServer(InetSocketAddress address) {
    return "http://" + address.getHostName() + ":" + address.getPort() + "/";
  }
}
