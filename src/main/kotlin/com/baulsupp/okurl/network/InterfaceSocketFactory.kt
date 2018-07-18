package com.baulsupp.okurl.network

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.net.UnknownHostException
import javax.net.SocketFactory

class InterfaceSocketFactory(private val localAddress: InetAddress) : SocketFactory() {
  private val systemFactory = SocketFactory.getDefault()

  override fun createSocket(): Socket {
    val s = systemFactory.createSocket()
    s.bind(InetSocketAddress(localAddress, 0))
    return s
  }

  override fun createSocket(host: String, port: Int): Socket {
    return systemFactory.createSocket(host, port, localAddress, 0)
  }

  override fun createSocket(address: InetAddress, port: Int): Socket {
    return systemFactory.createSocket(address, port, localAddress, 0)
  }

  override fun createSocket(
    host: String,
    port: Int,
    localAddr: InetAddress,
    localPort: Int
  ): Socket {
    return systemFactory.createSocket(host, port, localAddr, localPort)
  }

  override fun createSocket(
    address: InetAddress,
    port: Int,
    localAddr: InetAddress,
    localPort: Int
  ): Socket {
    return systemFactory.createSocket(address, port, localAddr, localPort)
  }

  companion object {

    fun byName(ipOrInterface: String): SocketFactory? {
      val localAddress = try {
        // example 192.168.0.51
        InetAddress.getByName(ipOrInterface)
      } catch (uhe: UnknownHostException) {
        // example en0
        val networkInterface = NetworkInterface.getByName(ipOrInterface) ?: return null

        networkInterface.inetAddresses.nextElement()
      }

      return InterfaceSocketFactory(localAddress)
    }
  }
}
