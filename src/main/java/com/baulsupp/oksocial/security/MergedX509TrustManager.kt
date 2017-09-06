package com.baulsupp.oksocial.security

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

import com.google.common.collect.Lists.newArrayList
import java.util.Arrays.asList

class MergedX509TrustManager(private val managers: List<X509TrustManager>) : X509TrustManager {

    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        throw UnsupportedOperationException()
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        val exceptions = newArrayList<CertificateException>()

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

    @Throws(CertificateException::class)
    fun bestException(exceptions: List<CertificateException>): CertificateException {
        if (exceptions.size > 0) {
            // last is probably system keystore
            throw exceptions[exceptions.size - 1]
        } else {
            throw CertificateException("no X509TrustManager to check")
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        val certificates = newArrayList<X509Certificate>()

        for (tm in managers) {
            certificates.addAll(asList(*tm.acceptedIssuers))
        }

        return certificates.toTypedArray()
    }
}
