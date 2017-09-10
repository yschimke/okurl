package com.baulsupp.oksocial.services.twitter

import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.google.common.base.Splitter
import java.util.*
import java.util.Optional.of

class TwitterServiceDefinition : ServiceDefinition<TwitterCredentials> {
    override fun apiHost(): String {
        return "api.twitter.com"
    }

    override fun serviceName(): String {
        return "Twitter API"
    }

    override fun shortName(): String {
        return "twitter"
    }

    override fun apiDocs(): Optional<String> {
        return of("https://apps.twitter.com/")
    }

    override fun accountsLink(): Optional<String> {
        return of("https://apps.twitter.com/")
    }

    override fun parseCredentialsString(s: String): TwitterCredentials {
        val list = Splitter.on(",").splitToList(s)

        if (list.size != 5) {
            throw IllegalStateException("can't split '$s'")
        }

        return TwitterCredentials(list[0], list[1], list[2], list[3], list[4])
    }

    override fun formatCredentialsString(credentials: TwitterCredentials): String {
        return StringJoiner(",")
                .add(credentials.username)
                .add(credentials.consumerKey)
                .add(credentials.consumerSecret)
                .add(credentials.token)
                .add(credentials.secret)
                .toString()
    }
}
