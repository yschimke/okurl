package com.baulsupp.okurl.services.symphony

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.kotlin.JSON
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.request
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.AbstractServiceDefinition
import com.baulsupp.okurl.services.symphony.model.SessionInfo
import com.baulsupp.okurl.services.symphony.model.TokenResponse
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class SymphonyAuthInterceptor : AuthInterceptor<SymphonyCredentials>() {
  override val serviceDefinition = object : AbstractServiceDefinition<SymphonyCredentials>(
    "symphony.com", "Symphony", "symphony",
    "https://rest-api.symphony.com/"
  ) {
    override fun parseCredentialsString(s: String): SymphonyCredentials {
      return SymphonyCredentials.parse(s)
    }

    override fun formatCredentialsString(credentials: SymphonyCredentials): String = credentials.format()
  }

  override suspend fun intercept(chain: Interceptor.Chain, credentials: SymphonyCredentials): Response {
    var request = chain.request()

    if (credentials.sessionToken != null) {
      val builder = request.newBuilder().header("sessionToken", credentials.sessionToken)
        .header("keyManagerToken", credentials.keyToken.orEmpty())

      repairJsonRequests(request, builder)

      request = builder.build()
    }

    return chain.proceed(request)
  }

  private fun repairJsonRequests(request: Request, builder: Request.Builder) {
    if (request.header("Content-Type") == "application/x-www-form-urlencoded") {
      val buffer = Buffer()
      request.body!!.writeTo(buffer)

      // repair missing header
      if (buffer.size > 0 && (buffer[0] == '['.toByte() || buffer[0] == '{'.toByte())) {
        builder.header("Content-Type", "application/json")
      }
    }
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): SymphonyCredentials {
    val pod = Secrets.prompt("Symphony POD", "symphony.pod", "foundation-dev", false)
    val keystoreFile = Secrets.prompt(
      "Symphony Client ID",
      "symphony.keystore",
      System.getenv("HOME") + "/.symphony/keystore.p12",
      false
    )
    val password = Secrets.prompt("Symphony Password", "symphony.password", "", true)

    return auth(keystoreFile, password, client, pod)
  }

  private suspend fun auth(
    keystoreFile: String,
    password: String,
    client: OkHttpClient,
    pod: String
  ): SymphonyCredentials {
    val keystore = KeyStore.getInstance("PKCS12").apply {
      load(File(keystoreFile).inputStream(), password.toCharArray())
    }

    val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
    kmf.init(keystore, password.toCharArray())

    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    tmf.init(null as KeyStore?)

    val context = SSLContext.getInstance("TLS")
    context.init(kmf.keyManagers, tmf.trustManagers, SecureRandom())

    val ssf = context.socketFactory
    val tm = tmf.trustManagers[0] as X509TrustManager

    // TODO avoid leaking connections
    val authClient = client.newBuilder().sslSocketFactory(ssf, tm).build()

    val authResponse = authClient.query<TokenResponse>(request {
      url("https://$pod-api.symphony.com/sessionauth/v1/authenticate")
      post("{}".toRequestBody(JSON))
    })

    val keyResponse = authClient.query<TokenResponse>(request {
      url("https://$pod-api.symphony.com/keyauth/v1/authenticate")
      post("{}".toRequestBody(JSON))
    })

    return SymphonyCredentials(pod, keystoreFile, password, authResponse.token, keyResponse.token)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: SymphonyCredentials
  ): ValidatedCredentials {
    if (credentials.sessionToken == null) {
      return ValidatedCredentials()
    }

    val result = client.query<SessionInfo>(request {
      url("https://${credentials.pod}.symphony.com/pod/v2/sessioninfo")
      header("sessionToken", credentials.sessionToken)
    })

    return ValidatedCredentials(result.displayName, result.company)
  }

  override suspend fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter {
    return SymphonyUrlCompleter(pods(credentialsStore), completionVariableCache, client)
  }

  override fun canRenew(credentials: SymphonyCredentials): Boolean {
    return true
  }

  override suspend fun renew(client: OkHttpClient, credentials: SymphonyCredentials): SymphonyCredentials? {
    return auth(credentials.keystore, credentials.password, client, credentials.pod)
  }

  suspend fun pods(credentialsStore: CredentialsStore): List<String> {
    val pods = credentialsStore.findAllNamed(serviceDefinition).keys.toList()
    return listOf("foundation-dev") + pods
  }

  override suspend fun supportsUrl(url: HttpUrl, credentialsStore: CredentialsStore): Boolean =
    url.host.endsWith("symphony.com")

  override fun hosts(credentialsStore: CredentialsStore): Set<String> {
    // TODO make suspend function
    return runBlocking { SymphonyUrlCompleter.podsToHosts(pods(credentialsStore)) }
  }
}
