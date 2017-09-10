package com.baulsupp.oksocial.services.twitter.twurlrc

import com.baulsupp.oksocial.services.twitter.TwitterCredentials
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.google.common.base.Throwables
import java.io.File
import java.util.*

class TwurlCredentialsStore(val file: File) {

    fun readTwurlRc(): TwurlRc? {
        try {
            if (file.isFile) {
                val objectMapper = ObjectMapper(YAMLFactory())
                objectMapper.propertyNamingStrategy = PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES

                return objectMapper.readValue(file, TwurlRc::class.java)
            } else {
                return null
            }
        } catch (e: Exception) {
            throw Throwables.propagate(e)
        }

    }

    fun readCredentials(): Optional<TwitterCredentials> {
        val twurlRc = readTwurlRc()

        if (twurlRc != null) {
            val username = twurlRc.defaultProfile()[0]
            val consumerKey = twurlRc.defaultProfile()[1]

            return Optional.ofNullable(twurlRc.readCredentials(username, consumerKey))
        } else {
            return Optional.empty()
        }
    }

    companion object {
        var TWURL_STORE = TwurlCredentialsStore(File(System.getProperty("user.home"), ".twurlrc"))
    }
}
