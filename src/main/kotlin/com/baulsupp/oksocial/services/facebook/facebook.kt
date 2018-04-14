package com.baulsupp.oksocial.services.facebook

import com.baulsupp.oksocial.credentials.Token
import com.baulsupp.oksocial.kotlin.moshi
import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.kotlin.queryForString
import com.baulsupp.oksocial.services.facebook.model.PageableResult
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
