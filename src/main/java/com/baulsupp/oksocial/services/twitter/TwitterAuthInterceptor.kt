package com.baulsupp.oksocial.services.twitter

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.services.twitter.twurlrc.TwurlrcImport
import com.google.common.collect.Lists
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.util.UsageException
import java.io.IOException
import java.util.Optional
import java.util.concurrent.Future
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

import java.util.stream.Collectors.joining

class TwitterAuthInterceptor : AuthInterceptor<TwitterCredentials> {

    override fun serviceDefinition(): TwitterServiceDefinition {
        return TwitterServiceDefinition()
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain, credentials: TwitterCredentials): Response {
        var request = chain.request()

        val authHeader = Signature().generateAuthorization(request, credentials)
        request = request.newBuilder().addHeader("Authorization", authHeader).build()

        return chain.proceed(request)
    }

    @Throws(IOException::class)
    fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>,
                  authArguments: List<String>): TwitterCredentials {
        System.err.println("Authorising Twitter API")

        if (!authArguments.isEmpty() && authArguments[0] == "--twurlrc") {
            return TwurlrcImport.authorize(authArguments)
        }

        if (authArguments == Lists.newArrayList("--pin")) {
            val consumerKey = Secrets.prompt("Consumer Key", "twitter.consumerKey", "", false)
            val consumerSecret = Secrets.prompt("Consumer Secret", "twitter.consumerSecret", "", true)

            return PinAuthorizationFlow(client, outputHandler).authorise(consumerKey, consumerSecret)
        }

        if (!authArguments.isEmpty()) {
            throw UsageException(
                    "unexpected arguments to --authorize twitter: " + authArguments.stream()
                            .collect<String, *>(joining(" ")))
        }

        val consumerKey = Secrets.prompt("Consumer Key", "twitter.consumerKey", "", false)
        val consumerSecret = Secrets.prompt("Consumer Secret", "twitter.consumerSecret", "", true)

        return WebAuthorizationFlow(client, outputHandler).authorise(consumerKey, consumerSecret)
    }

    @Throws(IOException::class)
    override fun validate(client: OkHttpClient,
                          requestBuilder: Request.Builder, credentials: TwitterCredentials): Future<Optional<ValidatedCredentials>> {
        return JsonCredentialsValidator(
                TwitterUtil.apiRequest("/1.1/account/verify_credentials.json", requestBuilder),
                { map -> map.get("name") }).validate(client)
    }

    override fun hosts(): Collection<String> {
        return TwitterUtil.TWITTER_API_HOSTS
    }
}
