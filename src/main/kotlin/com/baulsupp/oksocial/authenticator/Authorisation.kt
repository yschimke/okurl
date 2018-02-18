package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.util.UsageException
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

class Authorisation(private val interceptor: ServiceInterceptor, private val credentialsStore: CredentialsStore,
                    private val client: OkHttpClient, private val outputHandler: OutputHandler<Response>) {

  @Throws(Exception::class)
  suspend fun authorize(auth: AuthInterceptor<*>?, token: String?,
                        authArguments: List<String>) {
    if (auth == null) {
      throw UsageException(
        "unable to find authenticator. Specify name from " + interceptor.names().joinToString(", "))
    }

    if (token != null) {
      storeCredentials(auth, token)
    } else {
      authRequest(auth, authArguments)
    }
  }

  private fun <T> storeCredentials(auth: AuthInterceptor<T>, token: String) {
    val credentials = auth.serviceDefinition().parseCredentialsString(token)
    credentialsStore[auth.serviceDefinition()] = credentials
  }

  suspend fun <T> authRequest(auth: AuthInterceptor<T>, authArguments: List<String>) {
    auth.serviceDefinition().accountsLink()?.let { outputHandler.info("Accounts: " + it) }

    val credentials = auth.authorize(client, outputHandler, authArguments)

    credentialsStore[auth.serviceDefinition()] = credentials

    Secrets.instance.saveIfNeeded()

    // TODO validate credentials
  }

  @Throws(IOException::class)
  suspend fun <T> renew(auth: AuthInterceptor<T>?) {
    if (auth == null) {
      throw UsageException(
        "unable to find authenticator. Specify name from " + interceptor.names().joinToString(", "))
    }

    val serviceDefinition = auth.serviceDefinition()

    val credentials = credentialsStore[serviceDefinition] ?: throw UsageException("no existing credentials")

    if (!auth.canRenew(credentials)) {
      throw UsageException("credentials not renewable")
    }

    val newCredentials = auth.renew(client, credentials) ?: throw UsageException("failed to renew")

    credentialsStore[serviceDefinition] = newCredentials
  }

  fun remove(auth: AuthInterceptor<*>?) {
    if (auth == null) {
      throw UsageException(
        "unable to find authenticator. Specify name from " + interceptor.names().joinToString(", "))
    }

    credentialsStore.remove(auth.serviceDefinition())
  }
}
