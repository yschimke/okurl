package com.baulsupp.oksocial.services.slack

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.Future

/**
 * https://api.slack.com/docs/oauth
 */
class SlackAuthInterceptor : AuthInterceptor<Oauth2Token> {
    override fun serviceDefinition(): Oauth2ServiceDefinition {
        return Oauth2ServiceDefinition("slack.com", "Slack API", "slack", "https://api.slack.com/",
                "https://api.slack.com/apps")
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
        var request = chain.request()

        val token = credentials.accessToken

        val newUrl = request.url().newBuilder().addQueryParameter("token", token).build()

        request = request.newBuilder().url(newUrl).build()

        // TODO check for ok=false?

        return chain.proceed(request)
    }

    @Throws(IOException::class)
    override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>,
                           authArguments: List<String>): Oauth2Token {
        System.err.println("Authorising Slack API")

        val clientId = Secrets.prompt("Slack Client Id", "slack.clientId", "", false)
        val clientSecret = Secrets.prompt("Slack Client Secret", "slack.clientSecret", "", true)
        val scopes = Secrets.promptArray("Scopes", "slack.scopes", SlackUtil.SCOPES)

        return SlackAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
    }

    @Throws(IOException::class)
    override fun validate(client: OkHttpClient,
                          requestBuilder: Request.Builder, credentials: Oauth2Token): Future<ValidatedCredentials> {
        return JsonCredentialsValidator(
                SlackUtil.apiRequest("/api/auth.test", requestBuilder), { it["user"] as String }).validate(
                client)
    }

    override fun hosts(): Set<String> {
        return SlackUtil.API_HOSTS
    }
}
