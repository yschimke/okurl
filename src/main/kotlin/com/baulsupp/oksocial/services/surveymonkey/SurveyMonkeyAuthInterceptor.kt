package com.baulsupp.oksocial.services.surveymonkey

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.completion.*
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.*
import java.util.concurrent.Future

/**
 * https://developer.surveymonkey.com/docs/authentication
 */
class SurveyMonkeyAuthInterceptor : AuthInterceptor<SurveyMonkeyToken> {
    override fun serviceDefinition(): SurveyMonkeyServiceDefinition {
        return SurveyMonkeyServiceDefinition()
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain, credentials: SurveyMonkeyToken): Response {
        var request = chain.request()

        val token = credentials.accessToken

        val newUrl = request.url().newBuilder().addQueryParameter("api_key", credentials.apiKey).build()
        request = request.newBuilder().addHeader("Authorization", "bearer " + token).url(newUrl).build()

        return chain.proceed(request)
    }

    @Throws(IOException::class)
    override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>,
                           authArguments: List<String>): SurveyMonkeyToken {
        System.err.println("Authorising SurveyMonkey API")

        val apiKey = Secrets.prompt("SurveyMonkey API Key", "surveymonkey.apiKey", "", false)
        val accessToken = Secrets.prompt("SurveyMonkey Access Token", "surveymonkey.accessToken", "", true)
        return SurveyMonkeyToken(apiKey, accessToken)
    }

    @Throws(IOException::class)
    override fun validate(client: OkHttpClient,
                          requestBuilder: Request.Builder, credentials: SurveyMonkeyToken): Future<ValidatedCredentials> {
        return JsonCredentialsValidator(
                SurveyMonkeyUtil.apiRequest("/v3/users/me", requestBuilder),
                { it["username"] as String }).validate(client)
    }

    @Throws(IOException::class)
    override fun apiCompleter(prefix: String, client: OkHttpClient,
                              credentialsStore: CredentialsStore, completionVariableCache: CompletionVariableCache): ApiCompleter {
        val urlList = UrlList.fromResource(name())

        val credentials = credentialsStore.readDefaultCredentials(serviceDefinition())

        val completer = BaseUrlCompleter(urlList.get(), hosts())

        if (credentials != null) {
            completer.withVariable("survey",
                    {
                        completionVariableCache.compute(name(), "surveys",
                                {
                                    CompletionQuery.getIds(client, "https://api.surveymonkey.net/v3/surveys", "data",
                                            "id")
                                })
                    })
        }

        return completer
    }

    override fun hosts(): Collection<String> {
        return SurveyMonkeyUtil.API_HOSTS
    }
}
