package com.baulsupp.okurl.okhttp

import okhttp3.CipherSuite
import okhttp3.CipherSuite.Companion.forJavaName
import okhttp3.ConnectionSpec
import okhttp3.TlsVersion
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import java.net.InetAddress
import kotlin.reflect.full.staticProperties
import kotlin.reflect.jvm.javaType

val AllConnectionSpec: ConnectionSpec =
  ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).allEnabledCipherSuites().allEnabledTlsVersions().build()
val RestrictedConnectionSpec: ConnectionSpec =
  ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2).cipherSuites(
    CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
    CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,
    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
    CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
  ).build()

val TLS13_CIPHER_SUITES = arrayOf(
  forJavaName("TLS_AES_128_GCM_SHA256"),
  forJavaName("TLS_AES_256_GCM_SHA384"),
  forJavaName("TLS_CHACHA20_POLY1305_SHA256"),
  forJavaName("TLS_AES_128_CCM_SHA256"),
  forJavaName("TLS_AES_256_CCM_8_SHA256")
)

val MODERN_TLS_13_SPEC = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
  .cipherSuites(*(TLS13_CIPHER_SUITES.toList() + ConnectionSpec.MODERN_TLS.cipherSuites!!.toList()).toTypedArray())
  .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
  .build()

val TLS_13_ONLY_SPEC = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
  .cipherSuites(*TLS13_CIPHER_SUITES)
  .tlsVersions(TlsVersion.TLS_1_3)
  .build()

enum class ConnectionSpecOption(vararg val specs: ConnectionSpec) {
  ALL(AllConnectionSpec),
  RESTRICTED_TLS(RestrictedConnectionSpec),
  TLS_13_ONLY(TLS_13_ONLY_SPEC),
  MODERN_TLS_13(MODERN_TLS_13_SPEC),
  MODERN_TLS(ConnectionSpec.MODERN_TLS),
  COMPATIBLE_TLS(ConnectionSpec.COMPATIBLE_TLS),
  CLEARTEXT(ConnectionSpec.CLEARTEXT);
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
    .addTrustedCertificate(heldCertificate.certificate)
    .build()
}
