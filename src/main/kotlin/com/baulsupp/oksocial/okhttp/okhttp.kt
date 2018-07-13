package com.baulsupp.oksocial.okhttp

import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.TlsVersion
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import java.net.InetAddress
import kotlin.reflect.full.staticProperties
import kotlin.reflect.jvm.javaType

val AllConnectionSpec: ConnectionSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).allEnabledCipherSuites().allEnabledTlsVersions().build()
val RestrictedConnectionSpec: ConnectionSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2).cipherSuites(CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
  CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,
  CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
  CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
  CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
  CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384).build()

enum class ConnectionSpecOption(val spec: ConnectionSpec) {
  ALL(AllConnectionSpec),
  RESTRICTED_TLS(RestrictedConnectionSpec),
  MODERN_TLS(ConnectionSpec.MODERN_TLS),
  COMPATIBLE_TLS(ConnectionSpec.COMPATIBLE_TLS),
  CLEARTEXT(ConnectionSpec.CLEARTEXT);
}

class CipherSuiteOption(s: String) {
  val suite: CipherSuite = CipherSuite.forJavaName(s)
}

class TlsVersionOption(s: String) {
  val version: TlsVersion = TlsVersion.forJavaName(s)
}

fun cipherSuites(): List<CipherSuite> =
  CipherSuite::class.staticProperties.filter { it.isFinal && it.returnType.javaType == CipherSuite::class.java }.map { it.get() as CipherSuite }


/** Returns an SSL client for this host's localhost address.  */
fun localhost(): HandshakeCertificates {
  // Generate a self-signed cert for the server to serve and the client to trust.
  val heldCertificate = HeldCertificate.Builder()
    .commonName("localhost")
    .addSubjectAlternativeName(InetAddress.getByName("localhost").canonicalHostName)
    .build()

  return HandshakeCertificates.Builder()
    .heldCertificate(heldCertificate)
    .addTrustedCertificate(heldCertificate.certificate())
    .build()
}
