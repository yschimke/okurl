package com.baulsupp.oksocial.services.facebook

import com.baulsupp.oksocial.apidocs.ApiDocPresenter
import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.output.OutputHandler
import java.io.IOException
import java.util.Arrays
import java.util.Optional
import java.util.concurrent.Future
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

import com.baulsupp.oksocial.services.facebook.FacebookUtil.apiRequest

class FacebookAuthInterceptor : AuthInterceptor<Oauth2Token> {
    override fun serviceDefinition(): Oauth2ServiceDefinition {
        return Oauth2ServiceDefinition("graph.facebook.com", "Facebook API", "facebook",
                "https://developers.facebook.com/docs/graph-api", "https://developers.facebook.com/apps/")
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
        var request = chain.request()

        val token = credentials.accessToken

        val newUrl = request.url().newBuilder().addQueryParameter("access_token", token).build()

        request = request.newBuilder().url(newUrl).build()

        return chain.proceed(request)
    }

    @Throws(IOException::class)
    fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>,
                  authArguments: List<String>): Oauth2Token {
        System.err.println("Authorising Facebook API")

        val clientId = Secrets.prompt("Facebook App Id", "facebook.appId", "", false)
        val clientSecret = Secrets.prompt("Facebook App Secret", "facebook.appSecret", "", true)
        val scopes = Secrets.promptArray("Scopes", "facebook.scopes",
                Arrays.asList("public_profile", "user_friends", "email"))

        if (scopes.contains("all")) {
            scopes.remove("all")
            scopes.addAll(FacebookUtil.ALL_PERMISSIONS)
        }

        return FacebookAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
    }

    private fun extract(map: Map<String, Any>): String {
        return "" + map["name"] + " (" + map["id"] + ")"
    }

    @Throws(IOException::class)
    override fun validate(client: OkHttpClient,
                          requestBuilder: Request.Builder, credentials: Oauth2Token): Future<Optional<ValidatedCredentials>> {
        return JsonCredentialsValidator(apiRequest("/me", requestBuilder), Function<Map<String, Any>, String> { this.extract(it) },
                apiRequest("/app", requestBuilder), Function<Map<String, Any>, String> { this.extract(it) }).validate(client)
    }

    override fun hosts(): Collection<String> {
        return FacebookUtil.API_HOSTS
    }

    @Throws(IOException::class)
    override fun apiCompleter(prefix: String, client: OkHttpClient,
                              credentialsStore: CredentialsStore, completionVariableCache: CompletionVariableCache): ApiCompleter {
        return FacebookCompleter(client, hosts())
    }

    @Throws(IOException::class)
    override fun apiDocPresenter(url: String): ApiDocPresenter {
        return FacebookApiDocPresenter(serviceDefinition())
    }
}
