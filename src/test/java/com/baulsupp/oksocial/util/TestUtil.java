package com.baulsupp.oksocial.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.Assume;

public class TestUtil {
  private static UnknownHostException cachedException;
  private static boolean initialised = false;

  public static synchronized void assumeHasNetwork() {
    if (!initialised) {
      try {
        InetAddress.getByName("www.google.com");
      } catch (UnknownHostException e) {
        cachedException = e;
      }
      initialised = true;
    }

    Assume.assumeNoException(cachedException);
  }
}
