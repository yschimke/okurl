package com.baulsupp.oksocial.services.facebook

import com.baulsupp.oksocial.credentials.Token
import com.baulsupp.oksocial.kotlin.edit
import com.baulsupp.oksocial.kotlin.moshi
import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.kotlin.queryForString
import com.baulsupp.oksocial.kotlin.request
import com.baulsupp.oksocial.kotlin.tokenSet
import com.baulsupp.oksocial.services.facebook.model.Metadata
import com.baulsupp.oksocial.services.facebook.model.MetadataResult
import com.baulsupp.oksocial.services.facebook.model.PageableResult
import com.baulsupp.oksocial.util.ClientException
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
    "https://graph.facebook.com/v2.11$path?fields=${fields.joinToString(",")}", tokenSet)
  return moshi.adapter<T>(T::class.java).fromJson(stringResult)!!
}

suspend inline fun <reified T> OkHttpClient.fbQuery(path: String, tokenSet: Token): T {
  val fields = fbFieldNames(T::class)

  return this.query(
    "https://graph.facebook.com/v2.11$path?fields=${fields.joinToString(",")}",
    tokenSet)
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

const val VERSION = "v2.11"

val API_HOSTS = setOf("graph.facebook.com", "www.facebook.com", "streaming-graph.facebook.com")
