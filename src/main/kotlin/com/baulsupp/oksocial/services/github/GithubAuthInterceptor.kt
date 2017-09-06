package com.baulsupp.oksocial.services.github

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.output.OutputHandler
import java.io.IOException
import java.util.Optional
import java.util.concurrent.Future
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator.fieldExtractor

/**
 * https://developer.github.com/docs/authentication
 */
class GithubAuthInterceptor : AuthInterceptor<Oauth2Token> {
    override fun serviceDefinition(): Oauth2ServiceDefinition {
        return Oauth2ServiceDefinition("api.github.com", "Github API", "github",
                "https://developer.github.com/v3/", "https://github.com/settings/developers")
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
        var request = chain.request()

        val token = credentials.accessToken

        request = request.newBuilder().addHeader("Authorization", "token " + token).build()

        return chain.proceed(request)
    }

    @Throws(IOException::class)
    fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>,
                  authArguments: List<String>): Oauth2Token {
        System.err.println("Authorising Github API")

        val clientId = Secrets.prompt("Github Client Id", "github.clientId", "", false)
        val clientSecret = Secrets.prompt("Github Client Secret", "github.clientSecret", "", true)
        val scopes = Secrets.promptArray("Scopes", "github.scopes", GithubUtil.SCOPES)

        return GithubAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
    }

    @Throws(IOException::class)
    override fun validate(client: OkHttpClient,
                          requestBuilder: Request.Builder, credentials: Oauth2Token): Future<Optional<ValidatedCredentials>> {
        return JsonCredentialsValidator(
                GithubUtil.apiRequest("/user", requestBuilder), AuthInterceptor.Companion.fieldExtractor("name")).validate(
                client)
    }

    override fun hosts(): Set<String> {
        return GithubUtil.API_HOSTS
    }
}
