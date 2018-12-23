package com.baulsupp.okurl.services

import com.baulsupp.okurl.authenticator.AuthInterceptor

interface ServiceLibrary {
  val services: Iterable<AuthInterceptor<*>>

  fun knownServices(): Set<String>
  fun findAuthInterceptor(name: String): AuthInterceptor<*>?
}
