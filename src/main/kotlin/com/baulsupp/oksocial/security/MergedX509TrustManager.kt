package com.baulsupp.oksocial.security

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class MergedX509TrustManager(private val managers: List<X509TrustManager>) : X509TrustManager {

  override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
    throw UnsupportedOperationException()
  }

  override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
    val exceptions = mutableListOf<CertificateException>()

    for (tm in managers) {
      try {
        tm.checkServerTrusted(chain, authType)
        return
      } catch (e: CertificateException) {
        exceptions.add(e)
      }
    }

    throw bestException(exceptions)
  }

  fun bestException(exceptions: List<CertificateException>): CertificateException {
    if (exceptions.isNotEmpty()) {
      // last is probably system keystore
      throw exceptions[exceptions.size - 1]
    } else {
      throw CertificateException("no X509TrustManager to check")
    }
  }

  override fun getAcceptedIssuers(): Array<X509Certificate> {
    val certificates = mutableListOf<X509Certificate>()

    for (tm in managers) {
      certificates.addAll(tm.acceptedIssuers.toList())
    }

    return certificates.toTypedArray()
  }
}
