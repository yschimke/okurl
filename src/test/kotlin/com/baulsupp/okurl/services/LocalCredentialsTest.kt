@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.baulsupp.okurl.services

import com.baulsupp.okurl.Main
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.AuthenticatingInterceptor
import com.baulsupp.okurl.credentials.CredentialFactory
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.i9n.TestCredentialsStore
import com.baulsupp.okurl.util.ClientException
import kotlinx.coroutines.debug.DebugProbes
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class LocalCredentialsTest {
  init {
    DebugProbes.install()
  }

  val main = Main().apply {
    credentialsStore = TestCredentialsStore()
    initialise()
  }

  @TestFactory
  fun localCredentials(): Iterable<DynamicTest> = runBlocking {
    val services = AuthenticatingInterceptor.defaultServices()
    val credentialsStore = CredentialFactory.createCredentialsStore()

    services.flatMap {
      testsForService(credentialsStore, it)
    }
  }

  private suspend fun <T : Any> testsForService(
    credentialsStore: CredentialsStore,
    it: AuthInterceptor<T>
  ): List<DynamicTest> {
    val x = credentialsStore.findAllNamed(it.serviceDefinition)

    return if (x.isEmpty()) {
      listOf(DynamicTest.dynamicTest(it.name()) {
        Assumptions.assumeFalse(true, "No credentials")
      })
    } else {
      x.entries.map { (k, v) ->
        DynamicTest.dynamicTest(it.name() + ": " + k) {
          testFunction(it, v)
        }
      }
    }
  }

  private fun <T> testFunction(it: AuthInterceptor<T>, v: T) = runBlocking {
    try {
      it.validate(main.client, v)
    } catch (e: ClientException) {
      // allow
    }
  }
}
