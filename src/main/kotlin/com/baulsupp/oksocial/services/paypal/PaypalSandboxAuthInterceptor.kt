package com.baulsupp.oksocial.services.paypal

import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.BaseUrlCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import okhttp3.OkHttpClient
import java.io.IOException

class PaypalSandboxAuthInterceptor : PaypalAuthInterceptor() {
    override fun shortName(): String {
        return "paypal-sandbox"
    }

    override fun host(): String {
        return "api.sandbox.paypal.com"
    }

    @Throws(IOException::class)
    override fun apiCompleter(prefix: String, client: OkHttpClient,
                              credentialsStore: CredentialsStore, completionVariableCache: CompletionVariableCache): ApiCompleter {
        val urlList = UrlList.fromResource("paypal").get()

        val testUrls = urlList.getUrls("")
                .map { s -> s.replace("api.paypal.com", host()) }

        return BaseUrlCompleter(UrlList(UrlList.Match.SITE, testUrls), hosts())
    }
}
