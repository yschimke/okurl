package com.baulsupp.oksocial.util;

import com.baulsupp.oksocial.credentials.CredentialFactory;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.mcdermottroe.apple.OSXKeychainException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import org.junit.Assume;
import org.junit.jupiter.api.Assumptions;

import static java.util.Optional.empty;

public class TestUtil {
  private static UnknownHostException cachedException;
  private static boolean initialised = false;
  private static CredentialsStore credentialsStore;

  public static synchronized void assumeHasNetwork() {
    initialise();

    Assumptions.assumeTrue(cachedException == null);
  }

  private static void initialise() {
    if (!initialised) {
      try {
        InetAddress.getByName("www.google.com");
      } catch (UnknownHostException e) {
        cachedException = e;
      }

      try {
        credentialsStore = CredentialFactory.createCredentialsStore(empty());
      } catch (OSXKeychainException e) {
      }

      initialised = true;
    }
  }

  public static synchronized void assumeHasToken(
      ServiceDefinition<? extends Object> serviceDefinition) {
    initialise();

    Optional<?> token = credentialsStore.readDefaultCredentials(serviceDefinition);

    Assume.assumeTrue(token.isPresent());
  }
}
