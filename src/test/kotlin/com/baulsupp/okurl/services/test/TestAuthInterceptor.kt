package com.baulsupp.okurl.services.test

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.apidocs.ApiDocPresenter
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class TestAuthInterceptor : AuthInterceptor<Oauth2Token>() {

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response = chain.proceed(chain.request())

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>, authArguments: List<String>): Oauth2Token =
    if (authArguments.isEmpty()) {
      Oauth2Token("testToken")
    } else {
      Oauth2Token(authArguments[0])
    }

  override suspend fun validate(client: OkHttpClient, credentials: Oauth2Token): ValidatedCredentials =
    ValidatedCredentials("aaa", null)

  override val serviceDefinition =
    Oauth2ServiceDefinition("localhost", "Test Service", "test", "https://docs.test.com", "https://apps.test.com")

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf("test.com", "api1.test.com")

  override fun apiDocPresenter(url: String, client: OkHttpClient): ApiDocPresenter = object : ApiDocPresenter {
    override suspend fun explainApi(url: String, outputHandler: OutputHandler<Response>, client: OkHttpClient, tokenSet: Token) {
      outputHandler.info("Test: $url")
    }
  }
}
