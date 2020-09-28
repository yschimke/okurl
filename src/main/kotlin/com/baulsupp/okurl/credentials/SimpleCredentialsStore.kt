package com.baulsupp.okurl.credentials

import com.baulsupp.okurl.kotlin.listAdapter
import com.baulsupp.okurl.util.FileUtil
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import okio.source
import java.io.File

@JsonClass(generateAdapter = true)
data class StoredToken(
  val service: String,
  val tokenSet: String,
  val token: String
)

object SimpleCredentialsStore : CredentialsStore {
  val file = File(FileUtil.okurlSettingsDir, "tokens.json")
  val mutex = Mutex()

  val moshi by lazy {
    Moshi.Builder()
      .build()
  }

  var cachedCredentials: MutableList<StoredToken>? = null

  override suspend fun <T> get(
    serviceDefinition: ServiceDefinition<T>,
    tokenSet: String
  ): T? {
    return mutex.withLock {
      readCredentials().find { it.service == serviceDefinition.shortName() && it.tokenSet == tokenSet }
        ?.let {
          serviceDefinition.parseCredentialsString(it.token)
        }
    }
  }

  private suspend fun readCredentials(): MutableList<StoredToken> {
    if (cachedCredentials == null) {
      cachedCredentials = if (file.exists()) {
        withContext(Dispatchers.IO) {
          moshi.listAdapter<StoredToken>()
            .fromJson(
              file.source()
                .buffer()
            )
            ?.toMutableList()
        }
      } else {
        mutableListOf()
      }
    }
    return cachedCredentials!!
  }

  override suspend fun <T> set(
    serviceDefinition: ServiceDefinition<T>,
    tokenSet: String,
    credentials: T
  ) {
    mutex.withLock {
      readCredentials()

      val token = serviceDefinition.formatCredentialsString(credentials)
      cachedCredentials!!.removeIf { it.service == serviceDefinition.shortName() && it.tokenSet == tokenSet }
      cachedCredentials!!.add(StoredToken(serviceDefinition.shortName(), tokenSet, token))

      writeTokens()
    }
  }

  fun writeTokens() {
    file.sink()
      .buffer()
      .use {
        moshi.listAdapter<StoredToken>().indent("  ").toJson(it, cachedCredentials)
      }
  }

  override suspend fun <T> remove(
    serviceDefinition: ServiceDefinition<T>,
    tokenSet: String
  ) {
    mutex.withLock {
      readCredentials()

      cachedCredentials!!.removeIf { it.service == serviceDefinition.shortName() && it.tokenSet == tokenSet }

      writeTokens()
    }
  }
}
