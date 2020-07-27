package com.baulsupp.okurl.kotlin

import com.baulsupp.okurl.Main
import com.baulsupp.okurl.commands.CommandLineClient
import com.baulsupp.okurl.credentials.DefaultToken
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.util.ClientException
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.Color.GREEN
import org.fusesource.jansi.Ansi.Color.MAGENTA
import org.fusesource.jansi.Ansi.Color.RED
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Suppress("UNCHECKED_CAST")
inline fun <reified V> Moshi.mapAdapter() =
  this.adapter<Any>(
    Types.newParameterizedType(
      Map::class.java, String::class.java,
      V::class.java
    )
  ) as JsonAdapter<Map<String, V>>

@Suppress("UNCHECKED_CAST")
inline fun <reified V> Moshi.listAdapter() =
  this.adapter<Any>(
    Types.newParameterizedType(List::class.java, V::class.java)
  ) as JsonAdapter<List<V>>

suspend inline fun <reified T> OkHttpClient.query(url: String, tokenSet: Token = DefaultToken): T {
  return this.query(request(url, tokenSet))
}

suspend inline fun <reified T> OkHttpClient.query(request: Request): T {
  val stringResult = this.queryForString(request)

  return Main.moshi.adapter(T::class.java).fromJson(stringResult)!!
}

suspend inline fun <reified T> OkHttpClient.queryPages(
  url: String,
  crossinline paginator: T.() -> Pagination,
  tokenSet: Token = DefaultToken,
  pageLimit: Int = Int.MAX_VALUE
): List<T> = coroutineScope {
  var page = query<T>(url, tokenSet)
  val resultList = mutableListOf(page)

  var pages = paginator(page)

  while (pages !== End && resultList.size < pageLimit) {
    if (pages is Next) {
      page = query(pages.url, tokenSet)
      resultList.add(page)
      pages = paginator(page)
    } else if (pages is Rest) {
      val deferList = pages.urls.take(pageLimit).map {
        async {
          query<T>(it, tokenSet)
        }
      }
      deferList.forEach { resultList.add(it.await()) }
      break
    }
  }

  resultList
}

suspend inline fun <reified V> OkHttpClient.queryMap(request: Request): Map<String, V> {
  val stringResult = this.queryForString(request)

  return Main.moshi.mapAdapter<V>().fromJson(stringResult)!!
}

suspend inline fun <reified V> OkHttpClient.queryMap(
  url: String,
  tokenSet: Token = DefaultToken
): Map<String, V> =
  this.queryMap(request(url, tokenSet))

suspend inline fun <reified V> OkHttpClient.queryList(request: Request): List<V> {
  val stringResult = this.queryForString(request)

  return Main.moshi.listAdapter<V>().fromJson(stringResult)!!
}

sealed class Pagination

object End : Pagination()

data class Next(val url: String) : Pagination()

data class Rest(val urls: List<String>) : Pagination()

suspend inline fun <reified V> OkHttpClient.queryList(
  url: String,
  tokenSet: Token = DefaultToken
): List<V> =
  this.queryList(request(url, tokenSet))

suspend inline fun <reified V> OkHttpClient.queryOptionalMap(request: Request): Map<String, V>? {
  val stringResult = this.queryForString(request)

  return Main.moshi.mapAdapter<V>().fromJson(stringResult)
}

suspend inline fun <reified V> OkHttpClient.queryOptionalMap(
  url: String,
  tokenSet: Token = DefaultToken
): Map<String, V>? =
  this.queryOptionalMap(request(url, tokenSet))

suspend inline fun <reified T> OkHttpClient.queryMapValue(
  url: String,
  tokenSet: Token = DefaultToken,
  vararg keys: String
): T? =
  this.queryMapValue<T>(request(url, tokenSet), *keys)

@Suppress("UNCHECKED_CAST")
suspend inline fun <reified T> OkHttpClient.queryMapValue(
  request: Request,
  vararg keys: String
): T? {
  val queryMap = this.queryMap<Any>(request)

  val result = keys.fold(queryMap as Any) { map, key -> (map as Map<String, Any>).getValue(key) }

  return result as T
}

// TODO
fun HttpUrl.request(): Request = Request.Builder().url(this).build()

suspend fun OkHttpClient.queryForString(request: Request): String = execute(request).body!!.string()

suspend fun OkHttpClient.queryForString(url: String, tokenSet: Token = DefaultToken): String =
  this.queryForString(request(url, tokenSet))

suspend fun OkHttpClient.execute(request: Request): Response {
  val call = this.newCall(request)

  val response = call.await()

  if (!response.isSuccessful) {
    val responseString = response.body!!.string()

    val msg: String = if (responseString.isNotEmpty()) {
      responseString
    } else {
      response.statusMessage()
    }

    throw ClientException(msg, response.code)
  }

  return response
}

fun Response.statusMessage(): String = this.code.toString() + " " + this.message

fun OkHttpClient.warmup(vararg urls: String) {
  urls.forEach {
    val call = this.newCall(request(it, DefaultToken))
    call.enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        // ignore
      }

      override fun onResponse(call: Call, response: Response) {
        // ignore
        response.close()
      }
    })
  }
}

fun String.error(): String = color(RED)
fun String.warn(): String = color(MAGENTA)
fun String.success(): String = color(GREEN)
fun String.color(color: Ansi.Color): String = when {
  isInteractive() -> Ansi.ansi().reset().fg(color).a(this).reset().toString()
  else -> this
}

val JSON = "application/json".toMediaType()

fun form(init: FormBody.Builder.() -> Unit = {}): FormBody = FormBody.Builder().apply(init).build()

fun request(
  url: String? = null,
  tokenSet: Token = DefaultToken,
  init: Request.Builder.() -> Unit = {}
): Request = requestBuilder(url, tokenSet).apply(init).build()

fun requestBuilder(
  url: String? = null,
  tokenSet: Token = DefaultToken
): Request.Builder = Request.Builder().apply { if (url != null) url(url) }.tag(
  Token::class.java,
  tokenSet
)

fun Request.Builder.tokenSet(tokenSet: Token): Request.Builder = tag(Token::class.java, tokenSet)

fun Request.Builder.postJsonBody(body: Any) {
  val content = Main.moshi.adapter(body.javaClass).toJson(body)!!
  post(content.toRequestBody(JSON))
}

@Suppress("UNCHECKED_CAST", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
fun <T> List<T>.toJavaList(): java.util.List<T> {
  return this as java.util.List<T>
}

fun Request.edit(init: Request.Builder.() -> Unit = {}) = newBuilder().apply(init).build()
fun HttpUrl.edit(init: HttpUrl.Builder.() -> Unit = {}) = newBuilder().apply(init).build()

val isIntellij by lazy {
  try {
    CommandLineClient::class.java.classLoader.loadClass("com.intellij.rt.execution.application.AppMainV2")
    true
  } catch (e: Exception) {
    false
  }
}

fun isInteractive(): Boolean {
  return System.console() != null || isIntellij
}

fun LocalDateTime.toInstant(): Instant {
  return this.toInstant(OffsetDateTime.now().offset)
}

suspend fun Call.await(): Response {
  return suspendCancellableCoroutine { cont ->
    cont.invokeOnCancellation {
      cancel()
    }
    enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        if (!cont.isCompleted) {
          cont.resumeWithException(e)
        }
      }

      override fun onResponse(call: Call, response: Response) {
        if (!cont.isCompleted) {
          cont.resume(response)
        }
      }
    })
  }
}
