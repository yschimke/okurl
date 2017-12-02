package com.baulsupp.oksocial.services.facebook

import com.baulsupp.oksocial.kotlin.moshi
import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.kotlin.queryForString
import com.baulsupp.oksocial.services.facebook.model.PageableResult
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

inline suspend fun <reified I, reified T : PageableResult<I>> OkHttpClient.fbQueryList(
        path: String): T {
  val fields = fbFieldNames(I::class)

  val stringResult = this.queryForString(Request.Builder().url(
          "https://graph.facebook.com/v2.11$path?fields=" + fields.joinToString(",")).build())
  return moshi.adapter<T>(T::class.java).fromJson(stringResult)!!
}

inline suspend fun <reified T> OkHttpClient.fbQuery(path: String): T {
  val fields = fbFieldNames(T::class)

  return this.query(
          "https://graph.facebook.com/v2.11$path?fields=${fields.joinToString(",")}")
}

fun fbFieldNames(kClass: KClass<*>): Collection<String> {
  return kClass.memberProperties.map { it.name }.toSet().filterNot { it == "metadata" }
}
