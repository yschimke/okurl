package com.baulsupp.oksocial.services.instagram

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.Arrays
import java.util.concurrent.Future

class InstagramAuthInterceptor : AuthInterceptor<Oauth2Token> {
    override fun serviceDefinition(): Oauth2ServiceDefinition {
        return Oauth2ServiceDefinition("api.instagram.com", "Instagram API", "instagram",
                "https://www.instagram.com/developer/endpoints/",
                "https://www.instagram.com/developer/clients/manage/")
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
        var request = chain.request()

        val token = credentials.accessToken

        val newUrl = request.url().newBuilder().addQueryParameter("access_token", token).build()

        request = request.newBuilder().url(newUrl).build()

        return chain.proceed(request)
    }

    @Throws(IOException::class)
    override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>,
                           authArguments: List<String>): Oauth2Token {
        System.err.println("Authorising Instagram API")

        val clientId = Secrets.prompt("Instagram Client Id", "instagram.clientId", "", false)
        val clientSecret = Secrets.prompt("Instagram Client Secret", "instagram.clientSecret", "", true)
        val scopes = Secrets.promptArray("Scopes", "instagram.scopes",
                Arrays.asList("basic", "public_content", "follower_list", "comments", "relationships",
                        "likes"))

        return InstagramAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
    }

    @Throws(IOException::class)
    override fun validate(client: OkHttpClient,
                          requestBuilder: Request.Builder, credentials: Oauth2Token): Future<ValidatedCredentials> {
        return JsonCredentialsValidator(
                InstagramUtil.apiRequest("/v1/users/self", requestBuilder), this::getName).validate(client)
    }

    private fun getName(map: Map<String, Any>): String {
        val user = map["data"] as Map<String, Any>

        return user["full_name"] as String
    }

    override fun hosts(): Collection<String> {
        return InstagramUtil.API_HOSTS
    }
}
