package com.baulsupp.oksocial.services.test

import com.baulsupp.oksocial.Token
import com.baulsupp.oksocial.apidocs.ApiDocPresenter
import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class TestAuthInterceptor : AuthInterceptor<Oauth2Token>() {

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response = chain.proceed(chain.request())

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>, authArguments: List<String>): Oauth2Token =
      if (authArguments.isEmpty()) {
        Oauth2Token("testToken")
      } else {
        Oauth2Token(authArguments[0])
      }

  override suspend fun validate(client: OkHttpClient, credentials: Oauth2Token): ValidatedCredentials =
      ValidatedCredentials("aaa", null)

  override fun serviceDefinition(): ServiceDefinition<Oauth2Token> =
      Oauth2ServiceDefinition("localhost", "Test Service", "test", "https://docs.test.com", "https://apps.test.com")

  override fun hosts(): Set<String> = setOf("test.com", "api1.test.com")

  override fun apiDocPresenter(url: String): ApiDocPresenter = object : ApiDocPresenter {
    override suspend fun explainApi(url: String, outputHandler: OutputHandler<Response>, client: OkHttpClient, tokenSet: Token) {
      outputHandler.info("Test: $url")
    }
  }
}
