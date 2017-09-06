package com.baulsupp.oksocial.services.imgur

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import java.io.IOException
import java.util.Optional
import java.util.concurrent.Future
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

import com.baulsupp.oksocial.services.imgur.ImgurUtil.apiRequest

class ImgurAuthInterceptor : AuthInterceptor<Oauth2Token> {
    override fun serviceDefinition(): Oauth2ServiceDefinition {
        return Oauth2ServiceDefinition("api.imgur.com", "Imgur API", "imgur",
                "https://api.imgur.com/endpoints", "https://imgur.com/account/settings/apps")
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
        System.err.println("Authorising Imgur API")

        val clientId = Secrets.prompt("Imgur Client Id", "imgur.clientId", "", false)
        val clientSecret = Secrets.prompt("Imgur Client Secret", "imgur.clientSecret", "", true)

        return ImgurAuthFlow.login(client, outputHandler, clientId, clientSecret)
    }

    @Throws(IOException::class)
    override fun validate(client: OkHttpClient,
                          requestBuilder: Request.Builder, credentials: Oauth2Token): Future<Optional<ValidatedCredentials>> {
        return JsonCredentialsValidator(
                apiRequest("/3/account/me", requestBuilder),
                Function<Map<String, Any>, String> { this.getName(it) }).validate(client)
    }

    private fun getName(map: Map<String, Any>): String {
        val data = map["data"] as Map<String, Any>

        return data["url"] as String
    }

    override fun canRenew(result: Response): Boolean {
        return result.code() == 403
    }

    override fun canRenew(credentials: Oauth2Token): Boolean {
        return credentials.refreshToken.isPresent
                && credentials.clientId.isPresent
                && credentials.clientSecret.isPresent
    }

    @Throws(IOException::class)
    override fun renew(client: OkHttpClient, credentials: Oauth2Token): Optional<Oauth2Token> {
        val body = FormBody.Builder().add("refresh_token", credentials.refreshToken.get())
                .add("client_id", credentials.clientId.get())
                .add("client_secret", credentials.clientSecret.get())
                .add("grant_type", "refresh_token")
                .build()
        val request = Request.Builder().url("https://api.imgur.com/oauth2/token")
                .method("POST", body)
                .build()

        val responseMap = AuthUtil.makeJsonMapRequest(client, request)

        return Optional.of(Oauth2Token(responseMap["access_token"] as String,
                credentials.refreshToken.get(), credentials.clientId.get(),
                credentials.clientSecret.get()))
    }

    override fun hosts(): Collection<String> {
        return ImgurUtil.API_HOSTS
    }
}
