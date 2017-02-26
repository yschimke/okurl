package com.baulsupp.oksocial.credentials;

import com.google.common.base.Throwables;
import com.mcdermottroe.apple.OSXKeychain;
import com.mcdermottroe.apple.OSXKeychainException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OSXCredentialsStore implements CredentialsStore {
  private static Logger logger = Logger.getLogger(OSXCredentialsStore.class.getName());

  private final Optional<String> tokenSet;
  private final OSXKeychain keychain;

  public OSXCredentialsStore(Optional<String> tokenSet) throws OSXKeychainException {
    this.tokenSet = tokenSet;
    this.keychain = OSXKeychain.getInstance();
  }

  public OSXCredentialsStore() throws OSXKeychainException {
    this(Optional.empty());
  }

  @Override public <T> Optional<T> readDefaultCredentials(ServiceDefinition<T> serviceDefinition) {
    try {
      String pw = keychain.findGenericPassword(serviceDefinition.apiHost(), tokenKey());

      return Optional.ofNullable(serviceDefinition.parseCredentialsString(pw));
    } catch (OSXKeychainException e) {
      if ("The specified item could not be found in the keychain.".equals(e.getMessage())) {
        logger.log(Level.FINE,
            "No OSX Keychain entry for '" + serviceDefinition.apiHost() + "' '" + tokenKey() + "'");
      } else {
        logger.log(Level.FINE, "Failed to read from keychain", e);
      }

      return Optional.empty();
    }
  }

  @Override
  public <T> void storeCredentials(T credentials, ServiceDefinition<T> serviceDefinition) {
    String credentialsString = serviceDefinition.formatCredentialsString(credentials);

    try {
      keychain.deleteGenericPassword(serviceDefinition.apiHost(), tokenKey());
    } catch (OSXKeychainException e) {
      logger.log(Level.FINE, "No key to delete", e);
    }

    try {
      keychain.addGenericPassword(serviceDefinition.apiHost(), tokenKey(), credentialsString);
    } catch (OSXKeychainException e) {
      logger.log(Level.WARNING, "Failed to write to keychain", e);
      throw Throwables.propagate(e);
    }
  }

  private String tokenKey() {
    return "oauth" + tokenSet.map(s -> "." + s).orElse("");
  }
}
