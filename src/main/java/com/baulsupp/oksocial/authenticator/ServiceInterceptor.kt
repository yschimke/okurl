package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.credentials.CredentialsStore
import java.io.IOException
import java.util.ArrayList
import java.util.Optional
import java.util.ServiceLoader
import java.util.stream.Collectors
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class ServiceInterceptor(authClient: OkHttpClient, private val credentialsStore: CredentialsStore) : Interceptor {
    private val services = ArrayList<AuthInterceptor<*>>()
    private val authClient: OkHttpClient? = null

    init {
        this.authClient = authClient
        ServiceLoader.load(AuthInterceptor<*>::class.java, AuthInterceptor<*>::class.java.classLoader)
                .iterator().forEachRemaining(Consumer<AuthInterceptor> { services.add(it) })
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        for (interceptor in services) {
            if (interceptor.supportsUrl(chain.request().url())) {
                return intercept<Any>(interceptor, chain)
            }
        }

        return chain.proceed(chain.request())
    }

    @Throws(IOException::class)
    private fun <T> intercept(interceptor: AuthInterceptor<T>, chain: Interceptor.Chain): Response {
        var credentials = credentialsStore.readDefaultCredentials(interceptor.serviceDefinition())

        if (!credentials.isPresent) {
            credentials = interceptor.defaultCredentials()
        }

        if (credentials.isPresent) {
            val result = interceptor.intercept(chain, credentials.get())

            if (result.code() >= 400 && result.code() < 500) {
                if (interceptor.canRenew(result) && interceptor.canRenew(credentials.get())) {
                    val newCredentials = interceptor.renew(authClient, credentials.get())

                    if (newCredentials.isPresent) {
                        credentialsStore.storeCredentials(newCredentials.get(),
                                interceptor.serviceDefinition())
                    }
                }
            }

            // TODO retry request

            return result
        } else {
            return chain.proceed(chain.request())
        }
    }

    fun services(): List<AuthInterceptor<*>> {
        return services
    }

    fun getByName(authName: String): Optional<AuthInterceptor<*>> {
        return services.stream().filter { n -> n.name() == authName }.findFirst()
    }

    fun getByUrl(url: String): Optional<AuthInterceptor<*>> {
        val httpUrl = HttpUrl.parse(url)

        return if (httpUrl != null) {
            services.stream().filter { n -> n.supportsUrl(httpUrl) }.findFirst()
        } else Optional.empty()

    }

    fun findAuthInterceptor(nameOrUrl: String): Optional<AuthInterceptor<*>> {
        var auth = getByName(nameOrUrl)

        if (!auth.isPresent) {
            auth = getByUrl(nameOrUrl)
        }

        return auth
    }

    fun names(): List<String> {
        return services.stream().map<String>(Function<AuthInterceptor<*>, String> { it.name() }).collect<List<String>, Any>(Collectors.toList())
    }
}
