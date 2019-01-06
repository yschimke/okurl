package com.baulsupp.okurl.services.strava

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.edit
import com.baulsupp.okurl.kotlin.form
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.request
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.strava.model.Athlete
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.util.logging.Level

class StravaAuthInterceptor : Oauth2AuthInterceptor() {
  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {

    val clientId = Secrets.prompt("Strava Client Id", "strava.clientId", "", false)
    val clientSecret = Secrets.prompt("Strava Client Secret", "strava.clientSecret", "", true)

    val scopes = Secrets.promptArray(
      "Scopes", "strava.scopes", listOf(
        "read_all",
        "profile:read_all",
        "profile:write",
        "activity:read_all",
        "activity:write"
      )
    )

    return StravaAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override suspend fun supportsUrl(url: HttpUrl, credentialsStore: CredentialsStore): Boolean {
    return try {
      super.hosts(credentialsStore).contains(url.host())
    } catch (e: IOException) {
      logger.log(Level.WARNING, "failed getting hosts", e)
      false
    }
  }

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    return chain.proceed(chain.request().edit {
      addHeader("Authorization", "Bearer ${credentials.accessToken}")
    })
  }

  override val serviceDefinition = Oauth2ServiceDefinition(
    "www.strava.com", "Strava", "strava",
    "https://developers.strava.com/docs/reference/", "https://www.strava.com/settings/api"
  )

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials =
    ValidatedCredentials(client.query<Athlete>("https://www.strava.com/api/v3/athlete", TokenValue(credentials)).username)

  override fun canRenew(result: Response): Boolean {
    // Not working
    // {"message":"Authorization Error","errors":[{"resource":"AccessToken","field":"activity:read_permission","code":"missing"}]}
    // {"message":"Authorization Error","errors":[{"resource":"Athlete","field":"access_token","code":"invalid"}]}
//    return result.code() == 401 && result.body()?.source()?.peek()?.readUtf8()?.contains("\"field\":\"access_token\",\"code\":\"invalid\"") ?: false
    return false
  }

  override suspend fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token? {
    val responseMap = client.query<AuthResponse>(request("https://www.strava.com/oauth/token") {
      form {
        add("grant_type", "refresh_token")
        add("client_id", credentials.clientId!!)
        add("client_secret", credentials.clientSecret!!)
        add("refresh_token", credentials.refreshToken!!)
      }
    })

    return Oauth2Token(
      responseMap.access_token,
      responseMap.refresh_token,
      credentials.clientId,
      credentials.clientSecret
    )
  }
}
