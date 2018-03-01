package com.baulsupp.oksocial.okhttp

import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.TlsVersion

enum class ConnectionSpecOption(val spec: ConnectionSpec) {
  MODERN_TLS(ConnectionSpec.MODERN_TLS), COMPATIBLE_TLS(ConnectionSpec.COMPATIBLE_TLS), CLEARTEXT(ConnectionSpec.CLEARTEXT);
}

class CipherSuiteOption(s: String) {
  val suite: CipherSuite = CipherSuite.forJavaName(s)
}

class TlsVersionOption(s: String) {
  val version: TlsVersion = TlsVersion.forJavaName(s)
}
