package com.baulsupp.okurl.authenticator

import com.baulsupp.okurl.commands.CommandLineClient
import com.baulsupp.okurl.kotlin.client
import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.okurl.secrets.Secrets

class Authorisation(val main: CommandLineClient) {

  suspend fun authorize(
    auth: AuthInterceptor<*>?,
    token: String?,
    authArguments: List<String>,
    tokenSet: String
  ) {
    if (auth == null) {
      throw UsageException(
        "unable to find authenticator. Specify name from " + main.authenticatingInterceptor.names().joinToString(", "))
    }

    if (token != null) {
      storeCredentials(auth, token, tokenSet)
    } else {
      authRequest(auth, authArguments, tokenSet)
    }
  }

  private fun <T> storeCredentials(auth: AuthInterceptor<T>, token: String, tokenSet: String) {
    val credentials = auth.serviceDefinition.parseCredentialsString(token)
    main.credentialsStore.set(auth.serviceDefinition, tokenSet, credentials)
  }

  suspend fun <T> authRequest(auth: AuthInterceptor<T>, authArguments: List<String>, tokenSet: String) {
    auth.serviceDefinition.accountsLink()?.let { main.outputHandler.info("Accounts: $it") }

    val credentials = auth.authorize(client, main.outputHandler, authArguments)

    main.credentialsStore.set(auth.serviceDefinition, tokenSet, credentials)

    Secrets.instance.saveIfNeeded()

    // TODO validate credentials
  }

  suspend fun <T> renew(auth: AuthInterceptor<T>?, tokenSet: String) {
    if (auth == null) {
      throw UsageException(
        "unable to find authenticator. Specify name from " + main.authenticatingInterceptor.names().joinToString(", "))
    }

    val serviceDefinition = auth.serviceDefinition

    val credentials = main.credentialsStore.get(serviceDefinition, tokenSet)
      ?: throw UsageException("no existing credentials")

    if (!auth.canRenew(credentials)) {
      throw UsageException("credentials not renewable")
    }

    val newCredentials = auth.renew(client, credentials) ?: throw UsageException("failed to renew")

    main.credentialsStore.set(serviceDefinition, tokenSet, newCredentials)
  }

  fun remove(auth: AuthInterceptor<*>?, tokenSet: String) {
    if (auth == null) {
      throw UsageException(
        "unable to find authenticator. Specify name from " + main.authenticatingInterceptor.names().joinToString(", "))
    }

    main.credentialsStore.remove(auth.serviceDefinition, tokenSet)
  }
}
