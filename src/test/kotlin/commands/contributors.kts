#!/usr/bin/env okscript

import com.baulsupp.okurl.kotlin.*
import com.baulsupp.okurl.okhttp.MODERN_TLS_13_SPEC
import com.baulsupp.okurl.okhttp.TLS_13_ONLY_SPEC
import kotlinx.coroutines.runBlocking
import okhttp3.ConnectionSpec
import okhttp3.TlsVersion

data class Contributor(val login: String, val contributions: Int, val avatar_url: String, val url: String)

val repo = args.getOrElse(0) { "square/okhttp" }

val spec = ConnectionSpec.Builder(MODERN_TLS_13_SPEC).tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1).build()
val client2 = client.newBuilder().connectionSpecs(listOf(spec)).build()

val contributors = runBlocking {
  client2.queryList<Contributor>("https://api.github.com/repos/$repo/contributors")
}

contributors.forEach {
  println("${it.login}: ${it.contributions}")
}
