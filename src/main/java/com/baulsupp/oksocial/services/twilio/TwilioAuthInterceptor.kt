package com.baulsupp.oksocial.services.twilio

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.BasicCredentials
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.basic.BasicAuthServiceDefinition
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.BaseUrlCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.secrets.Secrets
import com.google.common.collect.Lists
import com.baulsupp.oksocial.output.OutputHandler
import java.io.IOException
import java.util.Optional
import java.util.concurrent.Future
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class TwilioAuthInterceptor : AuthInterceptor<BasicCredentials> {
    override fun serviceDefinition(): BasicAuthServiceDefinition {
        return BasicAuthServiceDefinition("api.twilio.com", "Twilio API", "twilio",
                "https://www.twilio.com/docs/api/rest", "https://www.twilio.com/console")
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain, credentials: BasicCredentials): Response {
        var request = chain.request()

        request = request.newBuilder()
                .addHeader("Authorization", Credentials.basic(credentials.user, credentials.password))
                .build()

        return chain.proceed(request)
    }

    fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>,
                  authArguments: List<String>): BasicCredentials {
        val user = Secrets.prompt("Twilio Account SID", "twilio.accountSid", "", false)
        val password = Secrets.prompt("Twilio Auth Token", "twilio.authToken", "", true)

        return BasicCredentials(user, password)
    }

    @Throws(IOException::class)
    override fun validate(client: OkHttpClient,
                          requestBuilder: Request.Builder, credentials: BasicCredentials): Future<Optional<ValidatedCredentials>> {
        return JsonCredentialsValidator(
                TwilioUtil.apiRequest("/2010-04-01/Accounts.json", requestBuilder),
                Function<Map<String, Any>, String> { this.getName(it) }).validate(client)
    }

    private fun getName(map: Map<String, Any>): String {
        val accounts = map["accounts"] as List<Map<String, Any>>

        return accounts[0]["friendly_name"] as String
    }

    @Throws(IOException::class)
    override fun apiCompleter(prefix: String, client: OkHttpClient,
                              credentialsStore: CredentialsStore, completionVariableCache: CompletionVariableCache): ApiCompleter {
        val urlList = UrlList.fromResource(name())

        val credentials = credentialsStore.readDefaultCredentials(serviceDefinition())

        val completer = BaseUrlCompleter(urlList.get(), hosts())

        credentials.ifPresent { basicCredentials ->
            completer.withVariable("AccountSid",
                    Lists.newArrayList(basicCredentials.user))
        }

        return completer
    }

    override fun hosts(): Collection<String> {
        return TwilioUtil.API_HOSTS
    }
}
