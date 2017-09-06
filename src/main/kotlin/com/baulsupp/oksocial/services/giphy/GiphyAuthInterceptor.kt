package com.baulsupp.oksocial.services.giphy

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.output.OutputHandler
import java.io.IOException
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class GiphyAuthInterceptor : AuthInterceptor<Oauth2Token> {
    override fun serviceDefinition(): Oauth2ServiceDefinition {
        return Oauth2ServiceDefinition("api.giphy.com", "Giphy API", "giphy",
                "https://github.com/Giphy/GiphyAPI", null!!)
    }

    override fun defaultCredentials(): Optional<Oauth2Token> {
        return Optional.of(Oauth2Token("dc6zaTOxFJmzC"))
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
        var request = chain.request()

        val token = credentials.accessToken

        val newUrl = request.url().newBuilder().addQueryParameter("api_key", token).build()

        request = request.newBuilder().url(newUrl).build()

        return chain.proceed(request)
    }

    @Throws(IOException::class)
    fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>,
                  authArguments: List<String>): Oauth2Token {
        System.err.println("Authorising Giphy API")

        val apiKey = Secrets.prompt("Giphy API Key", "giphy.apiKey", "", false)

        return Oauth2Token(apiKey)
    }

    @Throws(IOException::class)
    override fun validate(client: OkHttpClient,
                          requestBuilder: Request.Builder, credentials: Oauth2Token): Future<Optional<ValidatedCredentials>> {
        return CompletableFuture.completedFuture(Optional.of(ValidatedCredentials("?", null!!)))
    }

    override fun hosts(): Collection<String> {
        return GiphyUtil.API_HOSTS
    }
}
