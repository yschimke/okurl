package com.baulsupp.oksocial.security

import com.baulsupp.oksocial.output.util.UsageException
import okhttp3.CertificatePinner

import java.util.stream.Collectors.groupingBy

class CertificatePin(patternAndPin: String) {
    val pattern: String
    private val pin: String?

    init {
        val parts = patternAndPin.split(":".toRegex(), 2).toTypedArray()

        pattern = parts[0]
        pin = if (parts.size == 2) parts[1] else null
    }

    fun getPin(): String {
        if (pin == null) {
            throw UsageException(
                    "--certificatePin expects host:AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
        }

        return pin
    }

    companion object {

        fun buildFromCommandLine(pins: List<CertificatePin>): CertificatePinner {
            val builder = CertificatePinner.Builder()

            pins.stream().collect<Map<String, List<CertificatePin>>, Any>(groupingBy(Function<CertificatePin, String> { it.getPattern() })).forEach { host, pinList ->
                val pinArray = pinList.stream().map<String>(Function<CertificatePin, String> { it.getPin() }).toArray<String>(String[]::new  /* Currently unsupported in Kotlin */)

                builder.add(host, *pinArray)
            }

            return builder.build()
        }
    }
}