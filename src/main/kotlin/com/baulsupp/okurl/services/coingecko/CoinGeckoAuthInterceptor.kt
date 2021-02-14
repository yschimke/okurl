package com.baulsupp.okurl.services.coingecko

import com.baulsupp.okurl.authenticator.CompletionOnlyAuthInterceptor
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.okio.ClasspathFilesystem
import com.baulsupp.okurl.openapi.OpenApiCompleter
import com.baulsupp.okurl.openapi.readOpenAPI
import okhttp3.OkHttpClient
import okio.ExperimentalFileSystem
import okio.Path.Companion.toPath

/**
 * https://coingecko.com/
 */
@OptIn(ExperimentalFileSystem::class)
class CoinGeckoAuthInterceptor : CompletionOnlyAuthInterceptor(
  "api.coingecko.com", "CoinGecko", "coingecko",
  "https://www.coingecko.com/api/documentations/v3"
) {
  override suspend fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter =
    // OpenApiCompleter("https://www.coingecko.com/api/documentations/v3/swagger.json".toHttpUrl(), client)
  OpenApiCompleter {
    readOpenAPI(
      "/com/baulsupp/okurl/services/coingecko/swagger.json".toPath(), ClasspathFilesystem()
    )
  }
}
