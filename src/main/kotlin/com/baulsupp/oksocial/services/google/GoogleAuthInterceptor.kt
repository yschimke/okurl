package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.apidocs.ApiDocPresenter
import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.BaseUrlCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.output.OutputHandler
import java.io.IOException
import java.util.Optional
import java.util.concurrent.Future
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator.fieldExtractor
import java.util.stream.Collectors.toSet

/**
 * https://developer.google.com/docs/authentication
 */
class GoogleAuthInterceptor : AuthInterceptor<Oauth2Token> {
    private var hosts: Set<String>? = null
    private var discoveryIndex: DiscoveryIndex? = null

    override fun serviceDefinition(): Oauth2ServiceDefinition {
        return Oauth2ServiceDefinition("www.googleapis.com", "Google API", "google",
                "https://developers.google.com/", "https://console.developers.google.com/apis/credentials")
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
        var request = chain.request()

        val token = credentials.accessToken

        request = request.newBuilder().addHeader("Authorization", "Bearer " + token).build()

        return chain.proceed(request)
    }

    override fun supportsUrl(url: HttpUrl): Boolean {
        val host = url.host()

        return GoogleUtil.API_HOSTS.contains(host) || host.endsWith(".googleapis.com")
    }

    @Throws(IOException::class)
    fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>,
                  authArguments: List<String>): Oauth2Token {
        System.err.println("Authorising Google API")

        val clientId = Secrets.prompt("Google Client Id", "google.clientId", "", false)
        val clientSecret = Secrets.prompt("Google Client Secret", "google.clientSecret", "", true)
        val scopes = Secrets.promptArray("Scopes", "google.scopes", GoogleUtil.SCOPES)

        return GoogleAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
    }

    @Throws(IOException::class)
    override fun validate(client: OkHttpClient,
                          requestBuilder: Request.Builder, credentials: Oauth2Token): Future<Optional<ValidatedCredentials>> {
        return JsonCredentialsValidator(
                requestBuilder.url("https://www.googleapis.com/oauth2/v3/userinfo").build(),
                AuthInterceptor.Companion.fieldExtractor("name")).validate(client)
    }

    override fun canRenew(credentials: Oauth2Token): Boolean {
        return credentials.refreshToken.isPresent
                && credentials.clientId.isPresent
                && credentials.clientSecret.isPresent
    }

    @Throws(IOException::class)
    override fun renew(client: OkHttpClient, credentials: Oauth2Token): Optional<Oauth2Token> {
        val body = FormBody.Builder().add("client_id", credentials.clientId.get())
                .add("refresh_token", credentials.refreshToken.get())
                .add("client_secret", credentials.clientSecret.get())
                .add("grant_type", "refresh_token")
                .build()

        val request = Request.Builder().url("https://www.googleapis.com/oauth2/v4/token")
                .post(body)
                .build()

        val responseMap = AuthUtil.makeJsonMapRequest(client, request)

        return Optional.of(Oauth2Token(responseMap["access_token"] as String,
                credentials.refreshToken.get(), credentials.clientId.get(),
                credentials.clientSecret.get()))
    }

    @Synchronized
    @Throws(IOException::class)
    override fun hosts(): Collection<String> {
        if (hosts == null) {
            val urlList = UrlList.fromResource(name())

            hosts = urlList.get().getUrls("").stream().map<String>(Function<String, String> { this.extractHost(it) }).collect<Set<String>, Any>(toSet())
        }

        return hosts
    }

    private fun <R> extractHost(s: String): String {
        return HttpUrl.parse(s)!!.host()
    }

    @Throws(IOException::class)
    override fun apiCompleter(prefix: String, client: OkHttpClient,
                              credentialsStore: CredentialsStore, completionVariableCache: CompletionVariableCache): ApiCompleter {
        if (isPastHost(prefix)) {
            val discoveryPaths = DiscoveryIndex.loadStatic().getDiscoveryUrlForPrefix(prefix)

            return GoogleDiscoveryCompleter.forApis(DiscoveryRegistry.instance(client),
                    discoveryPaths)
        } else {
            val urlList = UrlList.fromResource(name()).get()

            return BaseUrlCompleter(urlList, hosts())
        }
    }

    private fun isPastHost(prefix: String): Boolean {
        return prefix.matches("https://.*/.*".toRegex())
    }

    @Throws(IOException::class)
    override fun apiDocPresenter(url: String): ApiDocPresenter {
        return DiscoveryApiDocPresenter(discoveryIndex())
    }

    @Synchronized
    @Throws(IOException::class)
    private fun discoveryIndex(): DiscoveryIndex {
        if (discoveryIndex == null) {
            discoveryIndex = DiscoveryIndex.loadStatic()
        }

        return discoveryIndex
    }
}
