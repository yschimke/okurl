package com.baulsupp.oksocial.services.dropbox

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.Future

/**
 * https://developer.dropbox.com/docs/authentication
 */
class DropboxAuthInterceptor : AuthInterceptor<Oauth2Token> {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("api.dropboxapi.com", "Dropbox API", "dropbox",
        "https://www.dropbox.com/developers", "https://www.dropbox.com/developers/apps")
  }

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    val builder = request.newBuilder().addHeader("Authorization", "Bearer " + token)
    request = builder.build()

    return chain.proceed(request)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>,
                         authArguments: List<String>): Oauth2Token {
    System.err.println("Authorising Dropbox API")

    val clientId = Secrets.prompt("Dropbox Client Id", "dropbox.clientId", "", false)
    val clientSecret = Secrets.prompt("Dropbox Client Secret", "dropbox.clientSecret", "", true)

    return DropboxAuthFlow.login(client, outputHandler, clientId, clientSecret)
  }

  override suspend fun validate(client: OkHttpClient,
                        requestBuilder: Request.Builder, credentials: Oauth2Token): Future<ValidatedCredentials> {
    val body = FormBody.create(MediaType.parse("application/json"), "null")
    return JsonCredentialsValidator(
        DropboxUtil.apiRequest("/2/users/get_current_account", requestBuilder)
            .newBuilder()
            .post(body)
            .build(),
        { it["email"]?.toString() ?: "unknown" }).validate(client)
  }

  override fun hosts(): Set<String> {
    return DropboxUtil.API_HOSTS
  }
}
