package com.baulsupp.okurl.authenticator

import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.okurl.commands.ToolSession
import com.baulsupp.okurl.secrets.Secrets

class Authorisation(val main: ToolSession) {

  suspend fun authorize(
    auth: AuthInterceptor<*>?,
    service: String,
    token: String?,
    authArguments: List<String> = listOf(),
    tokenSet: String
  ) {
    if (auth == null) {
      throw UsageException(
        "unable to find authenticator. Specify name from " + main.serviceLibrary.knownServices().joinToString(", ")
      )
    }

    if (token != null) {
      storeCredentials(auth, token, tokenSet)
    } else {
      authRequest(auth, service, tokenSet, authArguments)
    }
  }

  private suspend fun <T> storeCredentials(auth: AuthInterceptor<T>, token: String, tokenSet: String) {
    val credentials = auth.serviceDefinition.parseCredentialsString(token)
    main.credentialsStore.set(auth.serviceDefinition, tokenSet, credentials)
  }

  suspend fun <T> authRequest(auth: AuthInterceptor<T>, service: String, tokenSet: String, arguments: List<String> = listOf()) {
    auth.serviceDefinition.accountsLink()?.let { main.outputHandler.info("Accounts: $it") }

    val credentials = auth.authorize(main.client, main.outputHandler, arguments)

    main.credentialsStore.set(auth.serviceDefinition, tokenSet, credentials)

    Secrets.instance.saveIfNeeded()

    // TODO validate credentials
  }

  suspend fun <T> renew(auth: AuthInterceptor<T>?, tokenSet: String) {
    if (auth == null) {
      throw UsageException(
        "unable to find authenticator. Specify name from " + main.serviceLibrary.knownServices().joinToString(", ")
      )
    }

    val serviceDefinition = auth.serviceDefinition

    val credentials = main.credentialsStore.get(serviceDefinition, tokenSet)
      ?: throw UsageException("no existing credentials")

    if (!auth.canRenew(credentials)) {
      throw UsageException("credentials not renewable")
    }

    val newCredentials = auth.renew(main.client, credentials) ?: throw UsageException("failed to renew")

    main.credentialsStore.set(serviceDefinition, tokenSet, newCredentials)
  }

  suspend fun remove(auth: AuthInterceptor<*>?, tokenSet: String) {
    if (auth == null) {
      throw UsageException(
        "unable to find authenticator. Specify name from " + main.serviceLibrary.knownServices().joinToString(", ")
      )
    }

    main.credentialsStore.remove(auth.serviceDefinition, tokenSet)
  }
}
