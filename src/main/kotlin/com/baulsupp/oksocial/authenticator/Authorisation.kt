package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.util.UsageException
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.OkHttpClient
import okhttp3.Response

class Authorisation(val interceptor: ServiceInterceptor, val credentialsStore: CredentialsStore,
                    val client: OkHttpClient, val outputHandler: OutputHandler<Response>, val defaulttokenSet: String) {

  suspend fun authorize(auth: AuthInterceptor<*>?, token: String?,
                        authArguments: List<String>, tokenSet: String) {
    if (auth == null) {
      throw UsageException(
        "unable to find authenticator. Specify name from " + interceptor.names().joinToString(", "))
    }

    if (token != null) {
      storeCredentials(auth, token, tokenSet)
    } else {
      authRequest(auth, authArguments, tokenSet)
    }
  }

  private fun <T> storeCredentials(auth: AuthInterceptor<T>, token: String, tokenSet: String) {
    val credentials = auth.serviceDefinition().parseCredentialsString(token)
    credentialsStore.set(auth.serviceDefinition(), tokenSet, credentials)
  }

  suspend fun <T> authRequest(auth: AuthInterceptor<T>, authArguments: List<String>, tokenSet: String) {
    auth.serviceDefinition().accountsLink()?.let { outputHandler.info("Accounts: " + it) }

    val credentials = auth.authorize(client, outputHandler, authArguments)

    credentialsStore.set(auth.serviceDefinition(), tokenSet, credentials)

    Secrets.instance.saveIfNeeded()

    // TODO validate credentials
  }

  suspend fun <T> renew(auth: AuthInterceptor<T>?, tokenSet: String) {
    if (auth == null) {
      throw UsageException(
        "unable to find authenticator. Specify name from " + interceptor.names().joinToString(", "))
    }

    val serviceDefinition = auth.serviceDefinition()

    val credentials = credentialsStore.get(serviceDefinition, tokenSet)
      ?: throw UsageException("no existing credentials")

    if (!auth.canRenew(credentials)) {
      throw UsageException("credentials not renewable")
    }

    val newCredentials = auth.renew(client, credentials) ?: throw UsageException("failed to renew")

    credentialsStore.set(serviceDefinition, tokenSet, newCredentials)
  }

  fun remove(auth: AuthInterceptor<*>?, tokenSet: String) {
    if (auth == null) {
      throw UsageException(
        "unable to find authenticator. Specify name from " + interceptor.names().joinToString(", "))
    }

    credentialsStore.remove(auth.serviceDefinition(), tokenSet)
  }
}
