package com.baulsupp.okurl.credentials

import com.baulsupp.oksocial.output.isOSX

object CredentialFactory {
  fun createCredentialsStore(): CredentialsStore {
    return if (isOSX) {
      OSXCredentialsStore()
    } else {
      PreferencesCredentialsStore()
    }
  }
}
