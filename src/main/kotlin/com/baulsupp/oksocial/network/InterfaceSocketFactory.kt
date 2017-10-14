package com.baulsupp.oksocial.network

import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.net.SocketException
import java.net.UnknownHostException
import java.util.Optional
import java.util.Optional.empty
import java.util.Optional.of
import javax.net.SocketFactory

class InterfaceSocketFactory(private val localAddress: InetAddress) : SocketFactory() {
    private val systemFactory = SocketFactory.getDefault()

    @Throws(IOException::class)
    override fun createSocket(): Socket {
        val s = systemFactory.createSocket()
        s.bind(InetSocketAddress(localAddress, 0))
        return s
    }

    @Throws(IOException::class)
    override fun createSocket(host: String, port: Int): Socket {
        return systemFactory.createSocket(host, port, localAddress, 0)
    }

    @Throws(IOException::class)
    override fun createSocket(address: InetAddress, port: Int): Socket {
        return systemFactory.createSocket(address, port, localAddress, 0)
    }

    @Throws(IOException::class)
    override fun createSocket(host: String, port: Int, localAddr: InetAddress,
                              localPort: Int): Socket {
        return systemFactory.createSocket(host, port, localAddr, localPort)
    }

    @Throws(IOException::class)
    override fun createSocket(address: InetAddress, port: Int, localAddr: InetAddress,
                              localPort: Int): Socket {
        return systemFactory.createSocket(address, port, localAddr, localPort)
    }

    companion object {

        @Throws(SocketException::class)
        fun byName(ipOrInterface: String): Optional<SocketFactory> {
            val localAddress = try {
                // example 192.168.0.51
                InetAddress.getByName(ipOrInterface)
            } catch (uhe: UnknownHostException) {
                // example en0
                val networkInterface = NetworkInterface.getByName(ipOrInterface) ?: return empty()

                networkInterface.inetAddresses.nextElement()
            }

            return of(InterfaceSocketFactory(localAddress))
        }
    }
}
