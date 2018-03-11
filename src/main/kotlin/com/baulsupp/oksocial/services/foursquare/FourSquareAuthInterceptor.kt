package com.baulsupp.oksocial.services.foursquare

import com.baulsupp.oksocial.credentials.TokenValue
import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.kotlin.queryMap
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FourSquareAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("api.foursquare.com", "FourSquare API", "4sq",
      "https://developer.foursquare.com/docs/", "https://foursquare.com/developers/apps")
  }

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

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): Oauth2Token {

    val clientId = Secrets.prompt("FourSquare Application Id", "4sq.clientId", "", false)
    val clientSecret = Secrets.prompt("FourSquare Application Secret", "4sq.clientSecret", "", true)

    return FourSquareAuthFlow.login(client, outputHandler, clientId, clientSecret)
  }

  override suspend fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials {
    val map = client.queryMap<Any>("https://api.foursquare.com/v2/users/self?v=20160603",
      TokenValue(credentials))
    val userMap = (map["response"] as Map<String, Any>)["user"] as Map<String, Any>
    return ValidatedCredentials("${userMap["firstName"]} ${userMap["lastName"]}")
  }

  override fun hosts(): Set<String> = setOf("api.foursquare.com")
}
