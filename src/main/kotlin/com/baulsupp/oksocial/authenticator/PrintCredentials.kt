package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.util.ClientException
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class PrintCredentials(private val client: OkHttpClient, private val credentialsStore: CredentialsStore,
                       private val outputHandler: OutputHandler<*>, private val serviceInterceptor: ServiceInterceptor) {
  private val started: ZonedDateTime = ZonedDateTime.now()

  fun <T> printKnownCredentials(future: Future<ValidatedCredentials>,
                                a: AuthInterceptor<T>) {
    val sd = a.serviceDefinition()

    try {
      val left = 5000L - ZonedDateTime.now().until(started, ChronoUnit.MILLIS)
      val validated = future.get(left, TimeUnit.MILLISECONDS)

      printSuccess(sd, validated)
    } catch (e: InterruptedException) {
      printFailed(sd, e)
    } catch (e: TimeoutException) {
      printFailed(sd, e)
    } catch (e: ExecutionException) {
      printFailed(sd, e.cause!!)
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

  @Throws(Exception::class)
  fun showCredentials(arguments: List<String>, requestBuilder: () -> Request.Builder) {
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

  private fun validate(
      services: Iterable<AuthInterceptor<*>>, requestBuilder: () -> Request.Builder): Map<AuthInterceptor<*>, Future<ValidatedCredentials>> {
    return services.mapNotNull { sv ->
      val credentials = credentialsStore.readDefaultCredentials(sv.serviceDefinition())

      credentials?.let {
        val x = v(sv, requestBuilder, credentials)
        Pair(sv, x)
      }
    }.toMap()
  }

  // TODO fix up hackery
  private fun <T> v(sv: AuthInterceptor<T>, requestBuilder: () -> Request.Builder, credentials: Any?) =
      sv.validate(client, requestBuilder(), credentials as T)
}
