package com.baulsupp.oksocial.util

import okhttp3.Protocol
import java.io.IOException
import java.util.*

object ProtocolUtil {
    fun parseProtocolList(protocols: String): List<Protocol> {
        val protocolValues = ArrayList<Protocol>()

        try {
            protocols.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().mapTo(protocolValues) { Protocol.get(it) }
        } catch (e: IOException) {
            throw IllegalArgumentException(e)
        }

        if (!protocolValues.contains(Protocol.HTTP_1_1)) {
            protocolValues.add(Protocol.HTTP_1_1)
        }

        return protocolValues
    }
}
