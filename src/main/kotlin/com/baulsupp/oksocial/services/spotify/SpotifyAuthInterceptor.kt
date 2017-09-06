package com.baulsupp.oksocial.services.spotify

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.BasicCredentials
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
import com.baulsupp.oksocial.services.lyft.LyftUtil
import com.google.common.collect.Sets
import com.baulsupp.oksocial.output.OutputHandler
import java.io.IOException
import java.util.Collections
import java.util.Optional
import java.util.concurrent.Future
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

import java.util.Optional.of

class SpotifyAuthInterceptor : AuthInterceptor<Oauth2Token> {
    override fun serviceDefinition(): Oauth2ServiceDefinition {
        return Oauth2ServiceDefinition(host(), "Spotify API", "spotify",
                "https://developer.spotify.com/web-api/endpoint-reference/",
                "https://developer.spotify.com/my-applications/")
    }

    protected fun host(): String {
        return "api.spotify.com"
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
        var request = chain.request()

        val token = credentials.accessToken

        request = request.newBuilder().addHeader("Authorization", "Bearer " + token).build()

        return chain.proceed(request)
    }

    @Throws(IOException::class)
    fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>,
                  authArguments: List<String>): Oauth2Token {
        System.err.println("Authorising Spotify API")

        val clientId = Secrets.prompt("Spotify Client Id", "spotify.clientId", "", false)
        val clientSecret = Secrets.prompt("Spotify Client Secret", "spotify.clientSecret", "", true)

        val scopes = Secrets.promptArray("Scopes", "spotify.scopes", SpotifyUtil.SCOPES)

        return SpotifyAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
    }

    @Throws(IOException::class)
    override fun apiCompleter(prefix: String, client: OkHttpClient,
                              credentialsStore: CredentialsStore, completionVariableCache: CompletionVariableCache): ApiCompleter {
        return BaseUrlCompleter(UrlList.fromResource(name()).get(), hosts())
    }

    @Throws(IOException::class)
    override fun validate(client: OkHttpClient,
                          requestBuilder: Request.Builder, credentials: Oauth2Token): Future<Optional<ValidatedCredentials>> {
        return JsonCredentialsValidator(
                SpotifyUtil.apiRequest("/v1/me", requestBuilder),
                { map -> "" + map.get("display_name") }).validate(client)
    }

    override fun hosts(): Collection<String> {
        return Collections.unmodifiableSet(Sets.newHashSet(
                "api.spotify.com")
        )
    }

    override fun canRenew(credentials: Oauth2Token): Boolean {
        return credentials.refreshToken.isPresent
                && credentials.clientId.isPresent
                && credentials.clientSecret.isPresent
    }

    @Throws(IOException::class)
    override fun renew(client: OkHttpClient, credentials: Oauth2Token): Optional<Oauth2Token> {
        val tokenUrl = "https://accounts.spotify.com/api/token"

        val body = FormBody.Builder()
                .add("refresh_token", credentials.refreshToken.get())
                .add("grant_type", "refresh_token")
                .build()

        val request = Request.Builder().header("Authorization",
                Credentials.basic(credentials.clientId.get(), credentials.clientSecret.get()))
                .url(tokenUrl)
                .method("POST", body)
                .build()

        val responseMap = AuthUtil.makeJsonMapRequest(client, request)

        return of(Oauth2Token(responseMap["access_token"] as String,
                responseMap["refresh_token"] as String, credentials.clientId.get(),
                credentials.clientSecret.get()))
    }
}
