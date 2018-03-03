package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.Token
import com.baulsupp.oksocial.apidocs.ApiDocPresenter
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.BaseUrlCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.HostUrlCompleter
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.util.ClientException
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

abstract class AuthInterceptor<T> {
  open fun name(): String = serviceDefinition().shortName()

  open fun supportsUrl(url: HttpUrl): Boolean = try {
    hosts().contains(url.host())
  } catch (e: IOException) {
    logger.log(Level.WARNING, "failed getting hosts", e)
    false
  }

  abstract fun intercept(chain: Interceptor.Chain, credentials: T): Response

  abstract suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>, authArguments: List<String>): T

  abstract fun serviceDefinition(): ServiceDefinition<T>

  open suspend fun validate(client: OkHttpClient, credentials: T): ValidatedCredentials = ValidatedCredentials()

  open fun canRenew(result: Response): Boolean = result.code() == 401

  open fun canRenew(credentials: T): Boolean = false

  open suspend fun renew(client: OkHttpClient, credentials: T): T? = null

  abstract fun hosts(): Set<String>

  open fun apiCompleter(prefix: String, client: OkHttpClient, credentialsStore: CredentialsStore, completionVariableCache: CompletionVariableCache, tokenSet: Token): ApiCompleter =
    UrlList.fromResource(name())?.let { BaseUrlCompleter(it, hosts(), completionVariableCache) }
      ?: HostUrlCompleter(hosts())

  open fun defaultCredentials(): T? = null

  open fun apiDocPresenter(url: String): ApiDocPresenter {
    return object : ApiDocPresenter {
      override suspend fun explainApi(url: String, outputHandler: OutputHandler<Response>, client: OkHttpClient, tokenSet: Token) {
        val sd = serviceDefinition()

        outputHandler.info("service: " + sd.shortName())
        outputHandler.info("name: " + sd.serviceName())
        sd.apiDocs()?.let { outputHandler.info("docs: " + it) }
        sd.accountsLink()?.let { outputHandler.info("apps: " + it) }
      }
    }
  }

  open fun errorMessage(ce: ClientException): String {
    return ce.message ?: "response code ${ce.code}"
  }

  companion object {
    val logger = Logger.getLogger(AuthInterceptor::class.java.name)!!
  }
}
