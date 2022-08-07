package com.baulsupp.okurl.services.github

import com.baulsupp.okurl.Main
import com.baulsupp.okurl.authenticator.credentials
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.DefaultToken
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.kotlin.execute
import com.baulsupp.okurl.kotlin.listAdapter
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.queryList
import com.baulsupp.okurl.kotlin.readString
import com.baulsupp.okurl.kotlin.request
import com.baulsupp.okurl.openapi.OpenApiCompleter
import com.baulsupp.okurl.services.github.model.Repository
import com.baulsupp.okurl.services.github.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Response

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
          val ownerRepos =
            queryRepoPage("https://api.github.com/users/${requestDetails.owner}/repos")

          urls = urls.replace("repo", ownerRepos, keepTemplate = true)
        }
      } else {
        val cred = credentials(tokenSet, interceptor, credentialsStore)

        if (cred != null) {
          val owner = client.query<User>("https://api.github.com/user", tokenSet = tokenSet)
          val ownerRepos = queryRepoPage("https://api.github.com/user/repos")

          urls = urls.replace("owner", listOf(owner.login), keepTemplate = false)
          urls = urls.replace("repo", ownerRepos, keepTemplate = true)
        }
      }

      urls
    }
  }

  private suspend fun queryRepoPage(url: String) =
    client.queryResponsePages<Repository>(url).map {
      it.name
    }

  fun extract(url: HttpUrl): RequestDetails {
    return RequestDetails.fromUrl(url.toString())
  }
}

suspend inline fun <reified T> OkHttpClient.queryResponsePages(
  url: String,
  tokenSet: Token = DefaultToken
): List<T> = coroutineScope {
  val page1 = execute(request(url, tokenSet))

  val page1Results = async {
    val string = page1.body.readString()

    @Suppress("BlockingMethodInNonBlockingContext")
    Main.moshi.listAdapter<T>().fromJson(string)!!
  }

  val rest = nextPages(page1).map {
    async {
      queryList<T>("$url?page=$it")
    }
  }

  page1Results.await() + rest.awaitAll().flatten()
}

fun nextPages(page1: Response): List<Int> {
  val link = page1.headers["link"]

  if (link != null) {
    val refs = link.split(",\\s+".toRegex())
    val last = refs.find { it.endsWith("; rel=\"last\"") }

    if (last != null) {
      val lastPage = "page=(\\d)+".toRegex().find(last)
      if (lastPage != null) {
        return (2..lastPage.groupValues[1].toInt()).toList()
      }
    }
  }

  return listOf()
}
