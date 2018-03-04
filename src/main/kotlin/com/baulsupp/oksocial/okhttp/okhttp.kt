package com.baulsupp.oksocial.okhttp

import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.TlsVersion
import kotlin.reflect.full.staticProperties
import kotlin.reflect.jvm.javaType

enum class ConnectionSpecOption(val spec: ConnectionSpec) {
  //  RESTRICTED_TLS(ConnectionSpec.RESTRICTED_TLS),
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

fun cipherSuites(): List<CipherSuite> {
  return CipherSuite::class.staticProperties.filter { it.isFinal && it.returnType.javaType == CipherSuite::class.java }.map { it.get() as CipherSuite }
}
