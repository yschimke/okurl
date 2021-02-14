package com.baulsupp.okurl.services.github

import com.baulsupp.okurl.authenticator.credentials
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.DefaultToken
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.queryList
import com.baulsupp.okurl.openapi.OpenApiCompleter
import com.baulsupp.okurl.services.github.model.Repository
import com.baulsupp.okurl.services.github.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient

class GithubApiCompleter(
  client: OkHttpClient,
  val interceptor: GithubAuthInterceptor,
  val credentialsStore: CredentialsStore
) : ApiCompleter {
  val client = client.newBuilder()
    .addNetworkInterceptor {
      val response = it.proceed(it.request())
      response.newBuilder()
        .header("Cache-Control", "max-age=3600")
        .build()
    }
    .build()

  override suspend fun prefixUrls(): UrlList {
    return UrlList.hosts("api.github.com", "uploads.github.com")
  }

  override suspend fun siteUrls(
    url: HttpUrl,
    tokenSet: Token
  ): UrlList {
    val completer = OpenApiCompleter(
      "https://github.com/github/rest-api-description/blob/main/descriptions/api.github.com/api.github.com.yaml?raw=true".toHttpUrl(),
      client
    )

    return withContext(Dispatchers.Default) {
      val requestDetails = extract(url)

      val siteUrls = async { completer.siteUrls(url, DefaultToken) }

      var urls = siteUrls.await()

      if (requestDetails.owner != null) {
        urls = urls.replace("owner", listOf(requestDetails.owner), keepTemplate = false)

        if (requestDetails.repo != null) {
          urls = urls.replace("repo", listOf(requestDetails.repo), keepTemplate = false)
        } else {
          val ownerRepos = client.queryList<Repository>(
            "https://api.github.com/users/${requestDetails.owner}/repos"
          ).map {
            it.name
          }
          
          urls = urls.replace("repo", ownerRepos, keepTemplate = true)
        }
      } else {
        val cred = credentials(tokenSet, interceptor, credentialsStore)

        if (cred != null) {
          val owner = client.query<User>("https://api.github.com/user", tokenSet = tokenSet)
          val ownerRepos = client.queryList<Repository>("https://api.github.com/user/repos").map {
            it.name
          }

          urls = urls.replace("owner", listOf(owner.login), keepTemplate = false)
          urls = urls.replace("repo", ownerRepos, keepTemplate = true)
        }
      }

      urls
    }
  }

  fun extract(url: HttpUrl): RequestDetails {
    return RequestDetails.fromUrl(url.toString())
  }
}
