package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.apidocs.ApiDocPresenter
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.BaseUrlCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.HostUrlCompleter
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.Future
import java.util.logging.Level
import java.util.logging.Logger

interface AuthInterceptor<T> {
    fun name(): String = serviceDefinition().shortName()

    fun supportsUrl(url: HttpUrl): Boolean = try {
        hosts().contains(url.host())
    } catch (e: IOException) {
        logger.log(Level.WARNING, "failed getting hosts", e)
        false
    }

    @Throws(IOException::class)
    fun intercept(chain: Interceptor.Chain, credentials: T): Response

    @Throws(IOException::class)
    fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>, authArguments: List<String>): T

    fun serviceDefinition(): ServiceDefinition<T>

    fun validate(client: OkHttpClient,
                 requestBuilder: Request.Builder, credentials: T): Future<ValidatedCredentials>

    fun canRenew(result: Response): Boolean = result.code() == 401

    fun canRenew(credentials: T): Boolean = false

    @Throws(IOException::class)
    fun renew(client: OkHttpClient, credentials: T): T? = null

    @Throws(IOException::class)
    fun hosts(): Collection<String>

    @Throws(IOException::class)
    fun apiCompleter(prefix: String, client: OkHttpClient,
                     credentialsStore: CredentialsStore, completionVariableCache: CompletionVariableCache): ApiCompleter =
            UrlList.fromResource(name())?.let {
                BaseUrlCompleter(it, hosts())
            } ?: HostUrlCompleter(hosts())

    fun defaultCredentials(): T? = null

    @Throws(IOException::class)
    fun apiDocPresenter(url: String): ApiDocPresenter {
        return object : ApiDocPresenter {
            override fun explainApi(url: String, outputHandler: OutputHandler<Response>, client: OkHttpClient) {
                val sd = serviceDefinition()

                outputHandler.info("service: " + sd.shortName())
                outputHandler.info("name: " + sd.serviceName())
                sd.apiDocs()?.let { outputHandler.info("docs: " + it) }
                sd.accountsLink()?.let { outputHandler.info("apps: " + it) }
            }
        }
    }

    companion object {
        val logger = Logger.getLogger(AuthInterceptor::class.java.name)!!
    }

    // TODO fix up hackery
    fun cast(credentials: Any): T = credentials as T
}
