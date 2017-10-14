package com.baulsupp.oksocial.services.test

import com.baulsupp.oksocial.apidocs.ApiDocPresenter
import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.baulsupp.oksocial.output.OutputHandler
import io.github.vjames19.futures.jdk8.ImmediateFuture
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.Arrays
import java.util.concurrent.Future

class TestAuthInterceptor : AuthInterceptor<Oauth2Token> {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
        return chain.proceed(chain.request())
    }

    @Throws(IOException::class)
    override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>,
                           authArguments: List<String>): Oauth2Token {
        return if (authArguments.isEmpty()) {
            Oauth2Token("testToken")
        } else {
            Oauth2Token(authArguments[0])
        }
    }

    @Throws(IOException::class)
    override fun validate(client: OkHttpClient,
                          requestBuilder: Request.Builder,
                          credentials: Oauth2Token): Future<ValidatedCredentials> {
        return ImmediateFuture { ValidatedCredentials("aaa", null) }
    }

    override fun serviceDefinition(): ServiceDefinition<Oauth2Token> {
        return Oauth2ServiceDefinition("localhost", "Test Service", "test",
                "https://docs.test.com", "https://apps.test.com")
    }

    override fun hosts(): Collection<String> {
        return Arrays.asList("test.com", "api1.test.com")
    }

    override fun apiDocPresenter(url: String): ApiDocPresenter {
        return object : ApiDocPresenter {
            override fun explainApi(url: String, outputHandler: OutputHandler<Response>, client: OkHttpClient) {
                outputHandler.info("Test: " + url)
            }
        }
    }
}
