package com.baulsupp.oksocial.services.imgur

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.services.imgur.ImgurUtil.apiRequest
import okhttp3.*
import java.io.IOException
import java.util.concurrent.Future

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
    override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>,
                           authArguments: List<String>): Oauth2Token {
        System.err.println("Authorising Imgur API")

        val clientId = Secrets.prompt("Imgur Client Id", "imgur.clientId", "", false)
        val clientSecret = Secrets.prompt("Imgur Client Secret", "imgur.clientSecret", "", true)

        return ImgurAuthFlow.login(client, outputHandler, clientId, clientSecret)
    }

    @Throws(IOException::class)
    override fun validate(client: OkHttpClient,
                          requestBuilder: Request.Builder, credentials: Oauth2Token): Future<ValidatedCredentials> {
        return JsonCredentialsValidator(apiRequest("/3/account/me", requestBuilder), this::getName).validate(client)
    }

    private fun getName(map: Map<String, Any>): String {
        val data = map["data"] as Map<String, Any>

        return data["url"] as String
    }

    override fun canRenew(result: Response): Boolean = result.code() == 403

    override fun canRenew(credentials: Oauth2Token): Boolean = credentials.isRenewable()

    @Throws(IOException::class)
    override fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token {
        val body = FormBody.Builder().add("refresh_token", credentials.refreshToken)
                .add("client_id", credentials.clientId)
                .add("client_secret", credentials.clientSecret)
                .add("grant_type", "refresh_token")
                .build()
        val request = Request.Builder().url("https://api.imgur.com/oauth2/token")
                .method("POST", body)
                .build()

        val responseMap = AuthUtil.makeJsonMapRequest(client, request)

        // TODO check if refresh token in response?
        return Oauth2Token(responseMap["access_token"] as String,
                credentials.refreshToken, credentials.clientId,
                credentials.clientSecret)
    }

    override fun hosts(): Collection<String> {
        return ImgurUtil.API_HOSTS
    }
}
