package com.baulsupp.okurl.authenticator

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.commands.ToolSession
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.ServiceDefinition
import com.baulsupp.okurl.credentials.TokenSet
import com.baulsupp.okurl.util.ClientException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.Response
import java.io.IOException
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.logging.Level
import java.util.logging.Logger

class PrintCredentials(private val commandLineClient: ToolSession) {
  private val logger = Logger.getLogger(PrintCredentials::class.java.name)

  val outputHandler: OutputHandler<Response> = commandLineClient.outputHandler

  val credentialsStore: CredentialsStore = commandLineClient.credentialsStore

  private val started: ZonedDateTime = ZonedDateTime.now()

  private fun printKnownCredentials(future: Deferred<ValidatedCredentials>, key: Key) {
    try {
      val left = 2000L - ZonedDateTime.now().until(started, ChronoUnit.MILLIS)
      val validated = runBlocking {
        withTimeout(TimeUnit.MILLISECONDS.toMillis(left)) {
          future.await()
        }
      }

      printSuccess(key, validated)
    } catch (e: Exception) {
      printFailed(key, e)
    }
  }

  private fun printSuccess(key: Key, validated: ValidatedCredentials?) {
    val sd = key.auth.serviceDefinition
    outputHandler.info(
      "%-40s\t%-20s\t%-20s\t%-20s".format(
        displayName(sd), key.tokenSet.name, validated?.username
          ?: "-", validated?.clientName ?: "-"
      )
    )
  }

  fun displayName(sd: ServiceDefinition<*>) = sd.serviceName() + " (" + sd.shortName() + ")"

  private fun printFailed(key: Key, e: Throwable) {
    val sd = key.auth.serviceDefinition

    when (e) {
      is CancellationException -> outputHandler.info(
        "%-40s\t%-20s	%s".format(
          displayName(sd),
          key.tokenSet.name,
          "timeout"
        )
      )
      is TimeoutException -> outputHandler.info(
        "%-40s\t%-20s	%s".format(
          displayName(sd),
          key.tokenSet.name,
          "timeout"
        )
      )
      is ClientException -> outputHandler.info(
        "%-40s\t%-20s	%s".format(
          displayName(sd),
          key.tokenSet.name,
          key.auth.errorMessage(e)
        )
      )
      is IOException -> outputHandler.info("%-40s\t%-20s	%s".format(displayName(sd), key.tokenSet.name, e.toString()))
      else -> outputHandler.info("%-40s\t%-20s	%s".format(displayName(sd), key.tokenSet.name, e.toString()))
    }
  }

  suspend fun showCredentials(arguments: List<String>) {
    var services: Iterable<AuthInterceptor<*>> = commandLineClient.serviceLibrary.services
    val names = commandLineClient.defaultTokenSet?.let { listOf(it) }
      ?: commandLineClient.credentialsStore.names().map { TokenSet(it) }

    val full = !arguments.isEmpty()

    if (!arguments.isEmpty()) {
      services = arguments.mapNotNull { commandLineClient.serviceLibrary.findAuthInterceptor(it) }
    }

    val futures = validate(services, names)

    for ((key, future) in futures) {
      printKnownCredentials(future, key)
      if (full) {
        printCredentials(key)
      }
    }
  }

  data class Key(val auth: AuthInterceptor<*>, val tokenSet: TokenSet)

  private suspend fun printCredentials(key: Key) {
    val sd: ServiceDefinition<*> = key.auth.serviceDefinition
    val credentialsString = credentialsStore.get(sd, key.tokenSet)?.let { s(sd, it) }
      ?: "-"
    outputHandler.info(credentialsString)
  }

  suspend fun validate(
    services: Iterable<AuthInterceptor<*>>,
    names: List<TokenSet>
  ): Map<Key, Deferred<ValidatedCredentials>> {
    val pairs =
      services.flatMap { sv ->
        names.mapNotNull { name ->
          val credentials = try {
            credentialsStore.get(sv.serviceDefinition, name)
          } catch (e: Exception) {
            logger.log(Level.WARNING, "failed to read credentials for " + sv.name(), e)
            null
          }

          credentials?.let {
            val x = GlobalScope.async(Dispatchers.Default) {
              v(sv, credentials)
            }
            Pair(Key(sv, name), x)
          }
        }
      }
    return pairs.toMap()
  }

  suspend fun <T> v(sv: AuthInterceptor<T>, credentials: Any) =
    sv.validate(commandLineClient.client, sv.serviceDefinition.castToken(credentials))

  fun <T> s(sd: ServiceDefinition<T>, credentials: Any) =
    sd.formatCredentialsString(sd.castToken(credentials))
}
