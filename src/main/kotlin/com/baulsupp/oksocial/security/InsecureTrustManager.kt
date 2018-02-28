package com.baulsupp.oksocial.security

import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class InsecureTrustManager : X509TrustManager {

  override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
  }

  override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
  }

  override fun getAcceptedIssuers(): Array<X509Certificate> {
    return arrayOf()
  }
}
