package com.baulsupp.okurl.credentials

import com.baulsupp.schoutput.isOSX

object CredentialFactory {
  fun createCredentialsStore(): CredentialsStore {
    return if (isOSX) {
      OSXCredentialsStore()
    } else {
      PreferencesCredentialsStore()
    }
  }
}
