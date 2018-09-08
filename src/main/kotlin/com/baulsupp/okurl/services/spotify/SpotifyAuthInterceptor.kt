package com.baulsupp.okurl.services.spotify

import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.BaseUrlCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.moshi
import com.baulsupp.okurl.kotlin.queryMap
import com.baulsupp.okurl.kotlin.queryMapValue
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.spotify.model.ErrorResponse
import com.baulsupp.okurl.util.ClientException
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class SpotifyAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override val serviceDefinition = Oauth2ServiceDefinition("api.spotify.com", "Spotify API", "spotify",
    "https://developer.spotify.com/web-api/endpoint-reference/",
    "https://developer.spotify.com/my-applications/")

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    request = request.newBuilder().addHeader("Authorization", "Bearer $token").build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {

    val clientId = Secrets.prompt("Spotify Client Id", "spotify.clientId", "", false)
    val clientSecret = Secrets.prompt("Spotify Client Secret", "spotify.clientSecret", "", true)

    val scopes = Secrets.promptArray("Scopes", "spotify.scopes",
      listOf("playlist-read-private",
        "playlist-read-collaborative",
        "playlist-modify-public",
        "playlist-modify-private",
        "streaming",
        "ugc-image-upload",
        "user-follow-modify",
        "user-follow-read",
        "user-library-read",
        "user-library-modify",
        "user-read-private",
        "user-read-birthdate",
        "user-read-email",
        "user-top-read"))

    return SpotifyAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter {
    return BaseUrlCompleter(UrlList.fromResource(name())!!, hosts(), completionVariableCache)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials {
    return ValidatedCredentials(client.queryMapValue<String>("https://api.spotify.com/v1/me",
      TokenValue(credentials), "display_name"))
  }

  override fun hosts(): Set<String> = setOf("api.spotify.com")

  override fun canRenew(credentials: Oauth2Token): Boolean = credentials.isRenewable()

  override fun errorMessage(ce: ClientException): String {
    if (ce.code == 401) {
      try {
        val message = ce.responseMessage

        return moshi.adapter(ErrorResponse::class.java).fromJson(message)!!.error.message
      } catch (e: Exception) {
        // ignore
      }
    }

    return super.errorMessage(ce)
  }

  override suspend fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token? {
    val tokenUrl = "https://accounts.spotify.com/api/token"

    val body = FormBody.Builder()
      .add("refresh_token", credentials.refreshToken!!)
      .add("grant_type", "refresh_token")
      .build()

    val request = Request.Builder().header("Authorization",
      Credentials.basic(credentials.clientId!!, credentials.clientSecret!!))
      .url(tokenUrl)
      .method("POST", body)
      .build()

    val responseMap = client.queryMap<Any>(request)

    return Oauth2Token(responseMap["access_token"] as String,
      responseMap["refresh_token"] as String?, credentials.clientId,
      credentials.clientSecret)
  }
}
