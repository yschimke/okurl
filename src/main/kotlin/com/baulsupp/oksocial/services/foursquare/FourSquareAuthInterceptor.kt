package com.baulsupp.oksocial.services.foursquare

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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Future

class FourSquareAuthInterceptor : AuthInterceptor<Oauth2Token> {
    override fun serviceDefinition(): Oauth2ServiceDefinition {
        return Oauth2ServiceDefinition("api.foursquare.com", "FourSquare API", "4sq",
                "https://developer.foursquare.com/docs/", "https://foursquare.com/developers/apps")
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
        var request = chain.request()

        val token = credentials.accessToken

        val urlBuilder = request.url().newBuilder()
        urlBuilder.addQueryParameter("oauth_token", token)
        if (request.url().queryParameter("v") == null) {
            urlBuilder.addQueryParameter("v", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE))
        }

        request = request.newBuilder().url(urlBuilder.build()).build()

        return chain.proceed(request)
    }

    @Throws(IOException::class)
    override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>,
                           authArguments: List<String>): Oauth2Token {
        System.err.println("Authorising FourSquare API")

        val clientId = Secrets.prompt("FourSquare Application Id", "4sq.clientId", "", false)
        val clientSecret = Secrets.prompt("FourSquare Application Secret", "4sq.clientSecret", "", true)

        return FourSquareAuthFlow.login(client, outputHandler, clientId, clientSecret)
    }

    @Throws(IOException::class)
    override fun validate(client: OkHttpClient,
                          requestBuilder: Request.Builder, credentials: Oauth2Token): Future<ValidatedCredentials> {
        return JsonCredentialsValidator(
                FourSquareUtil.apiRequest("/v2/users/self?v=20160603", requestBuilder),
                { this.getName(it) }).validate(client)
    }

    private fun getName(map: Map<String, Any>): String {
        val user = (map["response"] as Map<String, Any>)["user"] as Map<String, Any>

        return "${user["firstName"]} ${user["lastName"]}"
    }

    override fun hosts(): Collection<String> {
        return FourSquareUtil.API_HOSTS
    }
}
