package com.baulsupp.okurl.authenticator

import okhttp3.Credentials

data class BasicCredentials(val user: String, val password: String) {
  fun header() = Credentials.basic(user, password)
}
