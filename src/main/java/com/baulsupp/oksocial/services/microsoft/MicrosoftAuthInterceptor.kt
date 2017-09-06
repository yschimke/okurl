package com.baulsupp.oksocial.services.microsoft

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.output.OutputHandler
import java.io.IOException
import java.util.Optional
import java.util.concurrent.Future
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator.fieldExtractor

/**
 * https://graph.microsoft.io/en-us/docs/authorization/app_authorization
 * http://graph.microsoft.io/en-us/docs/authorization/permission_scopes
 */
class MicrosoftAuthInterceptor : AuthInterceptor<Oauth2Token> {
    override fun serviceDefinition(): Oauth2ServiceDefinition {
        return Oauth2ServiceDefinition("graph.microsoft.com", "Microsoft API", "microsoft",
                "https://graph.microsoft.io/en-us/docs/get-started/rest",
                "https://apps.dev.microsoft.com/#/appList")
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
        System.err.println("Authorising Microsoft API")

        val clientId = Secrets.prompt("Microsoft Client Id", "microsoft.clientId", "", false)
        val clientSecret = Secrets.prompt("Microsoft Client Secret", "microsoft.clientSecret", "", true)

        return MicrosoftAuthFlow.login(client, outputHandler, clientId, clientSecret)
    }

    override fun canRenew(credentials: Oauth2Token): Boolean {
        return credentials.refreshToken.isPresent
                && credentials.clientId.isPresent
                && credentials.clientSecret.isPresent
    }

    @Throws(IOException::class)
    override fun renew(client: OkHttpClient, credentials: Oauth2Token): Optional<Oauth2Token> {

        val body = FormBody.Builder().add("grant_type", "refresh_token")
                .add("redirect_uri", "http://localhost:3000/callback")
                .add("client_id", credentials.clientId.get())
                .add("client_secret", credentials.clientSecret.get())
                .add("refresh_token", credentials.refreshToken.get())
                .add("resource", "https://graph.microsoft.com/")
                .build()

        val request = Request.Builder().url("https://login.microsoftonline.com/common/oauth2/token")
                .post(body)
                .build()

        val responseMap = AuthUtil.makeJsonMapRequest(client, request)

        return Optional.of(Oauth2Token(responseMap["access_token"] as String,
                credentials.refreshToken.get(), credentials.clientId.get(),
                credentials.clientSecret.get()))
    }

    @Throws(IOException::class)
    override fun validate(client: OkHttpClient,
                          requestBuilder: Request.Builder, credentials: Oauth2Token): Future<Optional<ValidatedCredentials>> {
        return JsonCredentialsValidator(
                MicrosoftUtil.apiRequest("/v1.0/me", requestBuilder),
                AuthInterceptor.Companion.fieldExtractor("displayName")).validate(
                client)
    }

    override fun hosts(): Set<String> {
        return MicrosoftUtil.API_HOSTS
    }
}
