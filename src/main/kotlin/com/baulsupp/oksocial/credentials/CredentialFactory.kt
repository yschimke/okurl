package com.baulsupp.oksocial.credentials

import com.baulsupp.oksocial.output.util.PlatformUtil

object CredentialFactory {
  fun createCredentialsStore(tokenSet: String?): CredentialsStore {
    return if (PlatformUtil.isOSX && OSXCredentialsStore.isAvailable()) {
      OSXCredentialsStore(tokenSet)
    } else {
      PreferencesCredentialsStore(tokenSet)
    }
  }
}
