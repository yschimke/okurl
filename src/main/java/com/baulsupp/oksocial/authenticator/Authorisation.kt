package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.util.UsageException
import java.io.IOException
import java.util.Optional
import okhttp3.OkHttpClient

import java.util.stream.Collectors.joining

class Authorisation(private val interceptor: ServiceInterceptor, private val credentialsStore: CredentialsStore,
                    private val client: OkHttpClient, private val outputHandler: OutputHandler<*>) {

    @Throws(Exception::class)
    fun authorize(auth: Optional<AuthInterceptor<*>>, token: Optional<String>,
                  authArguments: List<String>) {
        failIfNoAuthInterceptor(auth.isPresent)

        if (token.isPresent) {
            storeCredentials<*>(auth.get(), token.get())
        } else {
            authRequest<*>(auth.get(), authArguments)
        }
    }

    private fun failIfNoAuthInterceptor(present: Boolean) {
        if (!present) {
            throw UsageException(
                    "unable to find authenticator. Specify name from " + interceptor.names()
                            .stream()
                            .collect<String, *>(joining(", ")))
        }
    }

    private fun <T> storeCredentials(auth: AuthInterceptor<T>, token: String) {
        val credentials = auth.serviceDefinition().parseCredentialsString(token)
        credentialsStore.storeCredentials(credentials, auth.serviceDefinition())
    }

    @Throws(Exception::class)
    private fun <T> authRequest(auth: AuthInterceptor<T>, authArguments: List<String>) {

        auth.serviceDefinition().accountsLink().ifPresent { accountsLink -> outputHandler.info("Accounts: " + accountsLink) }

        val credentials = auth.authorize(client, outputHandler, authArguments)

        credentialsStore.storeCredentials(credentials, auth.serviceDefinition())

        Secrets.instance().saveIfNeeded()

        // TODO validate credentials
    }

    @Throws(IOException::class)
    fun <T> renew(auth: Optional<AuthInterceptor<T>>) {
        failIfNoAuthInterceptor(auth.isPresent)

        val serviceDefinition = auth.get().serviceDefinition()
        val credentials = credentialsStore.readDefaultCredentials(serviceDefinition)

        if (!credentials.isPresent) {
            throw UsageException("no existing credentials")
        }

        if (!auth.get().canRenew(credentials.get())) {
            throw UsageException("credentials not renewable")
        }

        val newCredentials = auth.get().renew(client, credentials.get())

        if (newCredentials.isPresent) {
            credentialsStore.storeCredentials(newCredentials.get(), serviceDefinition)
        } else {
            throw UsageException("failed to renew")
        }
    }
}
