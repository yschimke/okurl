package com.baulsupp.okurl.authenticator

import okhttp3.Credentials

data class BasicCredentials(val user: String, val password: String) {
  fun header(): String = Credentials.basic(user, password)
}
