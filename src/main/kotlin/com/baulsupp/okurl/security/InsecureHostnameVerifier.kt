package com.baulsupp.okurl.security

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

class InsecureHostnameVerifier : HostnameVerifier {
  override fun verify(s: String, sslSession: SSLSession): Boolean {
    return true
  }
}
