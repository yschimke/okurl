package com.baulsupp.oksocial.services.facebook

import com.baulsupp.oksocial.kotlin.client
import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.services.facebook.model.IdResult
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.OkHttpClient
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

data class User(override val id: String, val name: String,
        val email: String) : IdResult(id)

inline suspend fun <reified T> OkHttpClient.fbQuery(path: String): T {
  val fields = fbFieldNames(T::class)

  return this.query<T>(
          "https://graph.facebook.com/v2.11" + path + "?fields=" + fields.joinToString(","))
}

fun fbFieldNames(kClass: KClass<*>): Collection<String> {
  return kClass.memberProperties.map { it.name }.toSet().filterNot { it == "metadata" }
}

fun main(args: Array<String>) {
  runBlocking {
    println(client.fbQuery<User>("/me"))
  }
}
