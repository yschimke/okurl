package com.baulsupp.okurl.services.twitter

import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.BaseUrlCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.completion.UrlList.Match.EXACT
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.DefaultToken
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.openapi.OpenApiCompleter
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient

class TwitterApiCompleter(
  client: OkHttpClient,
  val credentialsStore: CredentialsStore,
  val completionVariableCache: CompletionVariableCache
) : ApiCompleter {
  val client = client.newBuilder()
    .addNetworkInterceptor {
      it.proceed(it.request())
        .newBuilder()
        .header("Cache-Control", "max-age=3600")
        .removeHeader("If-Modified-Since")
        .build()
    }
    .build()

  override suspend fun prefixUrls(): UrlList {
    return UrlList.hosts(TwitterUtil.TWITTER_HOSTS)
  }

  override suspend fun siteUrls(
    url: HttpUrl,
    tokenSet: Token
  ): UrlList {
    return supervisorScope {
      val defined = async {
        val fromResource = UrlList.fromResource("twitter")!!
        val completer = BaseUrlCompleter(fromResource, TwitterUtil.TWITTER_HOSTS, completionVariableCache)
        completer.siteUrls(url, tokenSet).urls
      }

      val lab1 = async {
        val completer = OpenApiCompleter(
          "https://api.twitter.com/labs/1/openapi.json".toHttpUrl(),
          client
        )
        completer.siteUrls(url, DefaultToken).urls
      }

      val lab2 = async {
        val completer = OpenApiCompleter(
          "https://api.twitter.com/labs/2/openapi.json".toHttpUrl(),
          client
        )
        completer.siteUrls(url, DefaultToken).urls
      }

      UrlList(EXACT, defined.await() + lab1.await() + lab2.await())
    }
  }
}
