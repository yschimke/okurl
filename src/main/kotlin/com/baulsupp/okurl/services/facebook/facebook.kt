package com.baulsupp.okurl.services.facebook

import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.kotlin.edit
import com.baulsupp.okurl.kotlin.moshi
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.queryForString
import com.baulsupp.okurl.kotlin.request
import com.baulsupp.okurl.kotlin.tokenSet
import com.baulsupp.okurl.services.facebook.model.Metadata
import com.baulsupp.okurl.services.facebook.model.MetadataResult
import com.baulsupp.okurl.services.facebook.model.PageableResult
import com.baulsupp.okurl.util.ClientException
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

suspend inline fun <reified I, reified T : PageableResult<I>> OkHttpClient.fbQueryList(
  path: String,
  tokenSet: Token
): T {
  val fields = fbFieldNames(I::class)

  val stringResult = this.queryForString(
    "https://graph.facebook.com/v7.0$path?fields=${fields.joinToString(",")}", tokenSet
  )
  return moshi.adapter<T>(T::class.java).fromJson(stringResult)!!
}

suspend inline fun <reified T> OkHttpClient.fbQuery(path: String, tokenSet: Token): T {
  val fields = fbFieldNames(T::class)

  return this.query(
    "https://graph.facebook.com/v7.0$path?fields=${fields.joinToString(",")}",
    tokenSet
  )
}

fun fbFieldNames(kClass: KClass<*>): Collection<String> {
  return kClass.memberProperties.map { it.name }.toSet().filterNot { it == "metadata" }
}

suspend fun getMetadata(client: OkHttpClient, requestUrl: HttpUrl, tokenSet: Token): Metadata? {
  val request = request {
    url(requestUrl.edit {
      addQueryParameter("metadata", "1")
    })
    tokenSet(tokenSet)
  }

  return try {
    val response = client.query<MetadataResult>(request)
    response.metadata
  } catch (ce: ClientException) {
    if (ce.code != 404) {
      throw ce
    }
    null
  }
}

const val VERSION = "v7.0"
