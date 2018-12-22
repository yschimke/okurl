package com.baulsupp.okurl.services.cooee

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.Jwt
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.edit
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.AbstractServiceDefinition
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class CooeeAuthInterceptor : AuthInterceptor<Jwt>() {
  override val serviceDefinition = object : AbstractServiceDefinition<Jwt>(
    "coo.ee", "Cooee API", "cooee",
    "https://coo.ee", "https://coo.ee"
  ) {
    override fun parseCredentialsString(s: String): Jwt = Jwt(s)

    override fun formatCredentialsString(credentials: Jwt): String = credentials.token
  }

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Jwt): Response {
    val request = chain.request()

    val newRequest = request.edit {
      addHeader("Authorization", "Bearer " + credentials.token)
    }

    return chain.proceed(newRequest)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Jwt =
    Jwt(Secrets.prompt("Cooee API Token", "cooee.token", "", true))

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Jwt
  ): ValidatedCredentials =
    ValidatedCredentials(
      client.query<UserInfo>(
        "https://api.coo.ee/api/v0/user",
        TokenValue(credentials)
      ).name
    )

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf("api.coo.ee")
}
