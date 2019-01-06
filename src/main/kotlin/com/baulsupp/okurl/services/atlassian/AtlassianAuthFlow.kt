package com.baulsupp.okurl.services.atlassian

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.NoToken
import com.baulsupp.okurl.kotlin.postJsonBody
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.request
import okhttp3.OkHttpClient
import okhttp3.Response
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class ExchangeRequest(
  val grant_type: String = "authorization_code",
  val client_id: String,
  val client_secret: String,
  val code: String,
  val redirect_uri: String
)

data class ExchangeResponse(
  val access_token: String,
  val scope: String?,
  val expires_in: Double,
  val token_type: String,
  val refresh_token: String?
)

data class RefreshRequest(
  val grant_type: String = "refresh_token",
  val client_id: String,
  val client_secret: String,
  val refresh_token: String
)

object AtlassianAuthFlow {
  suspend fun login(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    clientId: String,
    clientSecret: String,
    scopes: List<String>
  ): Oauth2Token {
    SimpleWebServer.forCode().use { s ->
      val serverUri = s.redirectUri

      val scopeString = URLEncoder.encode(scopes.joinToString(" "), StandardCharsets.UTF_8).replace("+", "%20")
      val serverUriEncoded = URLEncoder.encode(serverUri, StandardCharsets.UTF_8)

      val loginUrl =
        "https://auth.atlassian.com/authorize?audience=api.atlassian.com&client_id=$clientId&scope=$scopeString&redirect_uri=$serverUriEncoded&response_type=code&prompt=consent&state=secret"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val responseMap = client.query<ExchangeResponse>(request("https://auth.atlassian.com/oauth/token", NoToken) {
        postJsonBody(
          ExchangeRequest(
            client_id = clientId,
            client_secret = clientSecret,
            code = code,
            redirect_uri = serverUri
          )
        )
      })

      return Oauth2Token(responseMap.access_token, responseMap.refresh_token, clientId, clientSecret)
    }
  }
}
