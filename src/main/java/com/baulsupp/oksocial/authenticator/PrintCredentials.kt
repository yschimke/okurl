package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.baulsupp.oksocial.util.ClientException
import com.google.common.util.concurrent.Futures
import com.baulsupp.oksocial.output.OutputHandler
import java.io.IOException
import java.time.ZonedDateTime
import java.time.chrono.IsoChronology
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.HashMap
import java.util.Optional
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.function.Supplier
import okhttp3.OkHttpClient
import okhttp3.Request

import com.baulsupp.oksocial.output.util.FutureUtil.optionalStream
import java.util.Optional.empty
import java.util.stream.Collectors.toList

class PrintCredentials(private val client: OkHttpClient, private val credentialsStore: CredentialsStore,
                       private val outputHandler: OutputHandler<*>, private val serviceInterceptor: ServiceInterceptor) {
    private val started: ZonedDateTime

    init {

        this.started = ZonedDateTime.now()
    }

    fun <T> printKnownCredentials(future: Future<Optional<ValidatedCredentials>>,
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
            printFailed(sd, e.cause)
        }

    }

    private fun <T> printSuccess(sd: ServiceDefinition<T>, validated: Optional<ValidatedCredentials>) {
        outputHandler.info(
                String.format("%-40s\t%-20s\t%-20s", sd.serviceName() + " (" + sd.shortName() + ")",
                        validated.flatMap { v -> v.username }.orElse("-"),
                        validated.flatMap { v -> v.clientName }.orElse("-")))
    }

    private fun <T> printFailed(sd: ServiceDefinition<T>,
                                e: Throwable) {
        if (e is TimeoutException) {
            outputHandler.info(String.format("%-20s	%s", sd.serviceName(), "timeout"))
        } else if (e is ClientException) {
            outputHandler.info(String.format("%-20s	%s", sd.serviceName(), e.message))
        } else if (e is IOException) {
            outputHandler.info(String.format("%-20s	%s", sd.serviceName(), e.toString()))
        } else {
            outputHandler.info(String.format("%-20s	%s", sd.serviceName(), e.toString()))
        }
    }

    @Throws(Exception::class)
    fun showCredentials(arguments: List<String>, requestBuilder: Supplier<Request.Builder>) {
        var services: Iterable<AuthInterceptor<*>> = serviceInterceptor.services()

        val full = !arguments.isEmpty()

        if (!arguments.isEmpty()) {
            services = arguments.stream().flatMap { a -> optionalStream(serviceInterceptor.findAuthInterceptor(a)) }.collect<List<AuthInterceptor<*>>, Any>(
                    toList())
        }

        val futures = validate(services, requestBuilder)

        for (service in services) {
            val future = futures[service.name()]

            if (future != null) {
                printKnownCredentials<*>(future, service)
            } else {
                printSuccess<*>(service.serviceDefinition(), empty())
            }
            if (full) {
                printCredentials<*>(service)
            }
        }
    }

    private fun <T> printCredentials(service: AuthInterceptor<T>) {
        val sd = service.serviceDefinition()
        val credentialsString = credentialsStore.readDefaultCredentials(
                sd).map<String>(Function<T, String> { sd.formatCredentialsString(it) })
        outputHandler.info(credentialsString.orElse("-"))
    }

    private fun validate(
            services: Iterable<AuthInterceptor<*>>, requestBuilder: Supplier<Request.Builder>): Map<String, Future<Optional<ValidatedCredentials>>> {
        val result = HashMap<String, Future<Optional<ValidatedCredentials>>>()

        for (sv in services) {
            validate<*>(requestBuilder, result, sv)
        }

        return result
    }

    private fun <T> validate(requestBuilder: Supplier<Request.Builder>,
                             result: MutableMap<String, Future<Optional<ValidatedCredentials>>>, sv: AuthInterceptor<T>) {
        val credentials = credentialsStore.readDefaultCredentials(sv.serviceDefinition())

        if (credentials.isPresent) {
            try {
                val future = sv.validate(client, requestBuilder.get(), credentials.get())
                result.put(sv.name(), future)
            } catch (ioe: IOException) {
                result.put(sv.name(), Futures.immediateFailedFuture(ioe))
            }

        }
    }
}
