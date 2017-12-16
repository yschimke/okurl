package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.commands.CommandLineClient
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.util.ClientException
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withTimeout
import okhttp3.Response
import java.io.IOException
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.logging.Level
import java.util.logging.Logger

class PrintCredentials(private val commandLineClient: CommandLineClient) {
  private val logger = Logger.getLogger(PrintCredentials::class.java.name)

  val outputHandler: OutputHandler<Response> = commandLineClient.outputHandler!!

  val serviceInterceptor: ServiceInterceptor = commandLineClient.serviceInterceptor!!

  val credentialsStore: CredentialsStore = commandLineClient.credentialsStore!!

  private val started: ZonedDateTime = ZonedDateTime.now()

  fun <T> printKnownCredentials(future: Deferred<ValidatedCredentials>, a: AuthInterceptor<T>) {
    try {
      val left = 2000L - ZonedDateTime.now().until(started, ChronoUnit.MILLIS)
      val validated = runBlocking {
        withTimeout(left, TimeUnit.MILLISECONDS) {
          future.await()
        }
      }

      printSuccess(a, validated)
    } catch (e: Exception) {
      printFailed(a, e)
    }
  }

  private fun <T> printSuccess(a: AuthInterceptor<T>, validated: ValidatedCredentials?) {
    val sd = a.serviceDefinition()
    outputHandler.info("%-40s\t%-20s\t%-20s".format(sd.serviceName() + " (" + sd.shortName() + ")", validated?.username ?: "-", validated?.clientName ?: "-"))
  }

  private fun <T> printFailed(a: AuthInterceptor<T>, e: Throwable) {
    val sd = a.serviceDefinition()

    when (e) {
      is CancellationException -> outputHandler.info("%-20s	%s".format(sd.serviceName(), "timeout"))
      is TimeoutException -> outputHandler.info("%-20s	%s".format(sd.serviceName(), "timeout"))
      is ClientException -> outputHandler.info("%-20s	%s".format(sd.serviceName(), a.errorMessage(e)))
      is IOException -> outputHandler.info("%-20s	%s".format(sd.serviceName(), e.toString()))
      else -> outputHandler.info("%-20s	%s".format(sd.serviceName(), e.toString()))
    }
  }

  suspend fun showCredentials(arguments: List<String>) {
    var services: Iterable<AuthInterceptor<*>> = serviceInterceptor.services()

    val full = !arguments.isEmpty()

    if (!arguments.isEmpty()) {
      services = arguments.mapNotNull { serviceInterceptor.findAuthInterceptor(it) }
    }

    val futures = validate(services)

    for ((service, future) in futures) {
      printKnownCredentials(future, service)
      if (full) {
        printCredentials(service)
      }
    }
  }

  private fun <T> printCredentials(service: AuthInterceptor<T>) {
    val sd = service.serviceDefinition()
    val credentialsString = credentialsStore[sd]?.let({ sd.formatCredentialsString(it) }) ?: "-"
    outputHandler.info(credentialsString)
  }

  fun validate(
          services: Iterable<AuthInterceptor<*>>): Map<AuthInterceptor<*>, Deferred<ValidatedCredentials>> {
    return services.mapNotNull { sv ->
      val credentials = try {
        credentialsStore[sv.serviceDefinition()]
      } catch (e: Exception) {
        logger.log(Level.WARNING, "failed to read credentials for " + sv.name(), e)
        null
      }

      credentials?.let {
        val x = async(CommonPool) { v(sv, credentials) }
        Pair(sv, x)
      }
    }.toMap()
  }

  // TODO fix up hackery
  suspend fun <T> v(sv: AuthInterceptor<T>, credentials: Any?) =
          sv.validate(commandLineClient.client!!, credentials as T)
}
