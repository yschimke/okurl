package com.baulsupp.oksocial.credentials

import com.baulsupp.oksocial.output.util.PlatformUtil

object CredentialFactory {
  fun createCredentialsStore(): CredentialsStore {
    return if (PlatformUtil.isOSX) {
      OSXCredentialsStore()
    } else {
      PreferencesCredentialsStore()
    }
  }
}
