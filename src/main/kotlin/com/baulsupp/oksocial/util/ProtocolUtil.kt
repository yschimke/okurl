package com.baulsupp.oksocial.util

import java.io.IOException
import java.util.ArrayList
import okhttp3.Protocol

object ProtocolUtil {
    fun parseProtocolList(protocols: String): List<Protocol> {
        val protocolValues = ArrayList<Protocol>()

        try {
            for (protocol in protocols.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                protocolValues.add(Protocol.get(protocol))
            }
        } catch (e: IOException) {
            throw IllegalArgumentException(e)
        }

        if (!protocolValues.contains(Protocol.HTTP_1_1)) {
            protocolValues.add(Protocol.HTTP_1_1)
        }

        return protocolValues
    }
}
