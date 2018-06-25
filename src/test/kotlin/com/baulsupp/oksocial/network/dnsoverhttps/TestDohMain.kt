/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baulsupp.oksocial.network.dnsoverhttps

import okhttp3.Cache
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import okhttp3.dnsoverhttps.DnsOverHttps.UDPWIREFORMAT
import java.io.File
import java.io.IOException
import java.net.UnknownHostException
import java.security.Security
import java.util.Arrays

object TestDohMain {
  @Throws(IOException::class)
  @JvmStatic
  fun main(args: Array<String>) {
    Security.insertProviderAt(org.conscrypt.OpenSSLProvider(), 1)

    var bootstrapClient = OkHttpClient.Builder().build()

    var names = Arrays.asList("google.com", "graph.facebook.com", "sdflkhfsdlkjdf.ee")

    try {
      println("uncached\n********\n")
      var dnsProviders = DohProviders.providers(bootstrapClient, false)
      runBatch(dnsProviders, names)

      val dnsCache = Cache(File("./target/TestDohMain.cache." + System.currentTimeMillis()),
              (10 * 1024 * 1024).toLong())

      println("Bad targets\n***********\n")

      val url = HttpUrl.parse("https://dns.cloudflare.com/.not-so-well-known/run-dmc-query")
      val badProviders = listOf(DnsOverHttps.Builder().client(bootstrapClient)
              .url(url)
              .post(true)
              .contentType(UDPWIREFORMAT)
              .build())
      runBatch(badProviders, names)

      println("cached first run\n****************\n")
      names = Arrays.asList("google.com", "graph.facebook.com")
      bootstrapClient = bootstrapClient.newBuilder().cache(dnsCache).build()
      dnsProviders = DohProviders.providers(bootstrapClient, true)
      runBatch(dnsProviders, names)

      println("cached second run\n*****************\n")
      dnsProviders = DohProviders.providers(bootstrapClient, true)
      runBatch(dnsProviders, names)
    } finally {
      bootstrapClient.connectionPool().evictAll()
      bootstrapClient.dispatcher().executorService().shutdownNow()
      val cache = bootstrapClient.cache()
      cache?.close()
    }
  }

  private fun runBatch(dnsProviders: List<DnsOverHttps>, names: List<String>) {
    var time = System.currentTimeMillis()

    for (dns in dnsProviders) {
      println("Testing " + dns.url())

      for (host in names) {
        print("$host: ")
        System.out.flush()

        try {
          val results = dns.lookup(host)
          println(results)
        } catch (uhe: UnknownHostException) {
          var e: Throwable? = uhe

          while (e != null) {
            println(e.toString())

            e = e.cause
          }
        }

      }

      println()
    }

    time = System.currentTimeMillis() - time

    println("Time: " + time.toDouble() / 1000 + " seconds\n")
  }
}
