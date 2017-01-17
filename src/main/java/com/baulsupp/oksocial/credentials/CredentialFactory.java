package com.baulsupp.oksocial.credentials;

import com.baulsupp.oksocial.util.Util;
import com.mcdermottroe.apple.OSXKeychainException;
import java.util.Optional;

public class CredentialFactory {
  public static CredentialsStore createCredentialsStore(Optional<String> tokenSet)
      throws OSXKeychainException {
    if (Util.isOSX()) {
      return new OSXCredentialsStore(tokenSet);
    } else {
      return new PreferencesCredentialsStore(tokenSet);
    }
  }
}
