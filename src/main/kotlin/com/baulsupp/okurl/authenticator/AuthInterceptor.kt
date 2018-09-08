package com.baulsupp.okurl.authenticator

import com.baulsupp.okurl.apidocs.ApiDocPresenter
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.BaseUrlCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.HostUrlCompleter
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.ServiceDefinition
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.util.ClientException
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

abstract class AuthInterceptor<T> {
  open fun name(): String = serviceDefinition.shortName()

  open val priority: Int
    get() = 0

  open suspend fun supportsUrl(url: HttpUrl, credentialsStore: CredentialsStore): Boolean = try {
    hosts().contains(url.host())
  } catch (e: IOException) {
    logger.log(Level.WARNING, "failed getting hosts", e)
    false
  }

  abstract suspend fun intercept(chain: Interceptor.Chain, credentials: T): Response

  abstract suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>, authArguments: List<String>): T

  abstract val serviceDefinition: ServiceDefinition<T>

  open suspend fun validate(client: OkHttpClient, credentials: T): ValidatedCredentials = ValidatedCredentials()

  open fun canRenew(result: Response): Boolean = result.code() == 401

  open fun canRenew(credentials: T): Boolean = false

  open suspend fun renew(client: OkHttpClient, credentials: T): T? = null

  abstract fun hosts(): Set<String>

  open fun apiCompleter(prefix: String, client: OkHttpClient, credentialsStore: CredentialsStore, completionVariableCache: CompletionVariableCache, tokenSet: Token): ApiCompleter =
    UrlList.fromResource(name())?.let { BaseUrlCompleter(it, hosts(), completionVariableCache) }
      ?: HostUrlCompleter(hosts())

  open fun defaultCredentials(): T? = null

  open fun apiDocPresenter(url: String, client: OkHttpClient): ApiDocPresenter {
    return object : ApiDocPresenter {
      override suspend fun explainApi(url: String, outputHandler: OutputHandler<Response>, client: OkHttpClient, tokenSet: Token) {
        val sd = serviceDefinition

        outputHandler.info("service: " + sd.shortName())
        outputHandler.info("name: " + sd.serviceName())
        sd.apiDocs()?.let { outputHandler.info("docs: $it") }
        sd.accountsLink()?.let { outputHandler.info("apps: $it") }
      }
    }
  }

  open fun errorMessage(ce: ClientException): String {
    return ce.message ?: "response code ${ce.code}"
  }

  override fun toString(): String {
    return name()
  }

  companion object {
    val logger = Logger.getLogger(AuthInterceptor::class.java.name)!!
  }
}
