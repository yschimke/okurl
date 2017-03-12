package com.baulsupp.oksocial.credentials;

import com.mcdermottroe.apple.OSXKeychainException;
import ee.schimke.oksocial.output.util.PlatformUtil;
import java.util.Optional;

public class CredentialFactory {
  public static CredentialsStore createCredentialsStore(Optional<String> tokenSet)
      throws OSXKeychainException {
    if (PlatformUtil.isOSX()) {
      return new OSXCredentialsStore(tokenSet);
    } else {
      return new PreferencesCredentialsStore(tokenSet);
    }
  }
}
