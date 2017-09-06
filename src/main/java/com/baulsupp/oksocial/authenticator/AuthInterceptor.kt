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
import java.io.IOException
import java.util.Optional
import java.util.concurrent.Future
import java.util.logging.Level
import java.util.logging.Logger
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

import java.util.Optional.empty

interface AuthInterceptor<T> {

    fun name(): String {
        return serviceDefinition().shortName()
    }

    open fun supportsUrl(url: HttpUrl): Boolean {
        try {
            return hosts().contains(url.host())
        } catch (e: IOException) {
            logger.log(Level.WARNING, "failed getting hosts", e)
            return false
        }

    }

    @Throws(IOException::class)
    fun intercept(chain: Interceptor.Chain, credentials: T): Response

    @Throws(IOException::class)
    fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>, authArguments: List<String>): T

    fun serviceDefinition(): ServiceDefinition<T>

    @Throws(IOException::class)
    fun validate(client: OkHttpClient,
                 requestBuilder: Request.Builder, credentials: T): Future<Optional<ValidatedCredentials>>

    open fun canRenew(result: Response): Boolean {
        return result.code() == 401
    }

    open fun canRenew(credentials: T): Boolean {
        return false
    }

    @Throws(IOException::class)
    open fun renew(client: OkHttpClient, credentials: T): Optional<T> {
        return empty()
    }

    @Throws(IOException::class)
    fun hosts(): Collection<String>

    @Throws(IOException::class)
    open fun apiCompleter(prefix: String, client: OkHttpClient,
                          credentialsStore: CredentialsStore, completionVariableCache: CompletionVariableCache): ApiCompleter {
        val urlList = UrlList.fromResource(name())

        return if (urlList.isPresent) {
            BaseUrlCompleter(urlList.get(), hosts())
        } else {
            HostUrlCompleter(hosts())
        }
    }

    open fun defaultCredentials(): Optional<T> {
        return empty()
    }

    @Throws(IOException::class)
    open fun apiDocPresenter(url: String): ApiDocPresenter {
        return { url1, outputHandler, client ->
            val sd = serviceDefinition()

            outputHandler.info("service: " + sd.shortName())
            outputHandler.info("name: " + sd.serviceName())
            sd.apiDocs().ifPresent { d -> outputHandler.info("docs: " + d) }
            sd.accountsLink().ifPresent { d -> outputHandler.info("apps: " + d) }
        }
    }

    companion object {
        val logger = Logger.getLogger(AuthInterceptor<*>::class.java.name)
    }
}
