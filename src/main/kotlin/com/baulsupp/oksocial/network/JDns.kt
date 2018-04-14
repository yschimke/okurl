package com.baulsupp.oksocial.network

import com.baulsupp.oksocial.kotlin.client
import okhttp3.Dns
import java.net.InetAddress
import java.util.concurrent.Executors

class jDns: Dns {
  val executor = Executors.newCachedThreadPool()

  override fun lookup(hostname: String?): MutableList<InetAddress> {
    val resolver = OkHttpResolver(3, "google","https://dns.google.com/experimental", client)

//    resolver.start(executor, responses)
    return mutableListOf()
  }
}
