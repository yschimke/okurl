package com.baulsupp.oksocial.network

import io.netty.channel.EventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.resolver.ResolvedAddressTypes
import io.netty.resolver.dns.*
import okhttp3.Dns
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException
import java.util.*
import java.util.Arrays.stream
import java.util.concurrent.ExecutionException
import java.util.logging.Level
import java.util.logging.Logger
import java.util.stream.Collectors.joining
import kotlin.streams.toList

class NettyDns(private val group: EventLoopGroup, addressTypes: ResolvedAddressTypes?,
               dnsServers: List<InetSocketAddress>) : Dns {

    private val r: DnsNameResolver
    private val dnsServers: Iterable<InetSocketAddress>?

    init {
        this.dnsServers = dnsServers
        val builder = DnsNameResolverBuilder(this.group.next())
                .channelType(NioDatagramChannel::class.java)
                .optResourceEnabled(false)
                .maxQueriesPerResolve(3)
                .recursionDesired(true)

        if (logger.isLoggable(Level.FINEST)) {
            builder.traceEnabled(true)
        }

        if (dnsServers.size == 1) {
            builder.nameServerProvider(singleProvider(dnsServers[0]))
        } else {
            builder.nameServerProvider(multiProvider(dnsServers))
        }

        if (addressTypes != null) {
            builder.resolvedAddressTypes(addressTypes)
        }

        r = builder.build()
    }

    private fun multiProvider(dnsServers: List<InetSocketAddress>): DnsServerAddressStreamProvider =
            MultiDnsServerAddressStreamProvider(dnsServers.map { s -> singleProvider(s) })

    private fun singleProvider(address: InetSocketAddress): SingletonDnsServerAddressStreamProvider =
            SingletonDnsServerAddressStreamProvider(address)

    @Throws(UnknownHostException::class)
    override fun lookup(hostname: String): List<InetAddress> {
        val f = r.resolveAll(hostname)

        return try {
            val addresses = f.get()

            logger.fine("Dns ($hostname): " + addresses.stream()
                    .map<String>({ it.toString() })
                    .collect(joining(", ")))

            addresses
        } catch (e: InterruptedException) {
            throw UnknownHostException(e.toString())
        } catch (e: ExecutionException) {
            throw UnknownHostException(e.cause!!.message).initCause(
                    e.cause) as UnknownHostException
        }

    }

    companion object {
        private val logger = Logger.getLogger(NettyDns::class.java.name)

        fun byName(ipMode: IPvMode, eventLoopGroup: EventLoopGroup, dnsServers: String): Dns {
            val types = getInternetProtocolFamilies(ipMode)

            return NettyDns(eventLoopGroup, types, getDnsServers(dnsServers))
        }

        private fun getDnsServers(dnsServers: String?): List<InetSocketAddress> {
            if (dnsServers == null) {
                return DefaultDnsServerAddressStreamProvider.defaultAddressList()
            }

            return if (dnsServers == "google") {
                Arrays.asList(InetSocketAddress("8.8.8.8", 53),
                        InetSocketAddress("8.8.4.4", 53))
            } else stream(dnsServers.split(
                    ",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()).map { s ->
                InetSocketAddress(s, 53)
            }.toList()

        }

        private fun getInternetProtocolFamilies(ipMode: IPvMode): ResolvedAddressTypes? {
            return when (ipMode) {
                IPvMode.IPV6_FIRST -> ResolvedAddressTypes.IPV6_PREFERRED
                IPvMode.IPV4_FIRST -> ResolvedAddressTypes.IPV4_PREFERRED
                IPvMode.IPV6_ONLY -> ResolvedAddressTypes.IPV6_ONLY
                IPvMode.IPV4_ONLY -> ResolvedAddressTypes.IPV4_ONLY
                else -> null
            }
        }
    }
}
