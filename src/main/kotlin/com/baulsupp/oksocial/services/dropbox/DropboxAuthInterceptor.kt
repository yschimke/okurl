package com.baulsupp.oksocial.services.dropbox

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.kotlin.queryMapValue
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

/**
 * https://developer.dropbox.com/docs/authentication
 */
class DropboxAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("api.dropboxapi.com", "Dropbox API", "dropbox",
            "https://www.dropbox.com/developers", "https://www.dropbox.com/developers/apps")
  }

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    val builder = request.newBuilder().addHeader("Authorization", "Bearer " + token)

    if (request.method() == "GET") {
      builder.method("POST", RequestBody.create(MediaType.parse("application/json"), "{}"))
    }

    request = builder.build()

    return chain.proceed(request)
  }

  suspend override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): Oauth2Token {
    val clientId = Secrets.prompt("Dropbox Client Id", "dropbox.clientId", "", false)
    val clientSecret = Secrets.prompt("Dropbox Client Secret", "dropbox.clientSecret", "", true)

    return DropboxAuthFlow.login(client, outputHandler, clientId, clientSecret)
  }

  suspend override fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials {
    val body = FormBody.create(MediaType.parse("application/json"), "null")
    val request = Request.Builder().url("https://api.dropboxapi.com/2/users/get_current_account").post(body).build()
    return ValidatedCredentials(client.queryMapValue<String>(request, "email"))
  }

  override fun hosts(): Set<String> = setOf("api.dropboxapi.com", "content.dropboxapi.com")
}
