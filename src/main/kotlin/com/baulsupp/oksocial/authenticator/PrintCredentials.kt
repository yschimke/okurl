package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.util.ClientException
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class PrintCredentials(private val client: OkHttpClient, private val credentialsStore: CredentialsStore,
                       private val outputHandler: OutputHandler<Response>, private val serviceInterceptor: ServiceInterceptor) {
  private val started: ZonedDateTime = ZonedDateTime.now()

  fun <T> printKnownCredentials(future: Deferred<ValidatedCredentials>, a: AuthInterceptor<T>) {
    val sd = a.serviceDefinition()

    try {
      val left = 2000L - ZonedDateTime.now().until(started, ChronoUnit.MILLIS)
      val validated = runBlocking {
        withTimeout(left, TimeUnit.MILLISECONDS) {
          future.await()
        }
      }

      printSuccess(sd, validated)
    } catch (e: Exception) {
      printFailed(sd, e)
    }
  }

  private fun <T> printSuccess(sd: ServiceDefinition<T>, validated: ValidatedCredentials?) {
    outputHandler.info("%-40s\t%-20s\t%-20s".format(sd.serviceName() + " (" + sd.shortName() + ")", validated?.username ?: "-", validated?.clientName ?: "-"))
  }

  private fun <T> printFailed(sd: ServiceDefinition<T>,
                              e: Throwable) {
    when (e) {
      is TimeoutException -> outputHandler.info("%-20s	%s".format(sd.serviceName(), "timeout"))
      is ClientException -> outputHandler.info("%-20s	%s".format(sd.serviceName(), e.message))
      is IOException -> outputHandler.info("%-20s	%s".format(sd.serviceName(), e.toString()))
      else -> outputHandler.info("%-20s	%s".format(sd.serviceName(), e.toString()))
    }
  }

  suspend fun showCredentials(arguments: List<String>, requestBuilder: () -> Request.Builder) {
    var services: Iterable<AuthInterceptor<*>> = serviceInterceptor.services()

    val full = !arguments.isEmpty()

    if (!arguments.isEmpty()) {
      services = arguments.mapNotNull { serviceInterceptor.findAuthInterceptor(it) }
    }

    val futures = validate(services, requestBuilder)

    for ((service, future) in futures) {
      printKnownCredentials(future, service)
      if (full) {
        printCredentials(service)
      }
    }
  }

  private fun <T> printCredentials(service: AuthInterceptor<T>) {
    val sd = service.serviceDefinition()
    val credentialsString = credentialsStore.readDefaultCredentials(sd)?.let({ sd.formatCredentialsString(it) }) ?: "-"
    outputHandler.info(credentialsString)
  }

  suspend fun validate(
          services: Iterable<AuthInterceptor<*>>, requestBuilder: () -> Request.Builder): Map<AuthInterceptor<*>, Deferred<ValidatedCredentials>> {
    return services.mapNotNull { sv ->
      val credentials = credentialsStore.readDefaultCredentials(sv.serviceDefinition())

      credentials?.let {
        val x = async(CommonPool) { v(sv, requestBuilder, credentials) }
        Pair(sv, x)
      }
    }.toMap()
  }

  // TODO fix up hackery
  suspend fun <T> v(sv: AuthInterceptor<T>, requestBuilder: () -> Request.Builder, credentials: Any?) =
          sv.validate(client, requestBuilder(), credentials as T)
}
