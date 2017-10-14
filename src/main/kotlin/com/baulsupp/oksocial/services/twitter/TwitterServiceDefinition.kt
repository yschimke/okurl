package com.baulsupp.oksocial.services.twitter

import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.google.common.base.Splitter
import java.util.StringJoiner

class TwitterServiceDefinition : ServiceDefinition<TwitterCredentials> {
    override fun apiHost() = "api.twitter.com"

    override fun serviceName() = "Twitter API"

    override fun shortName() = "twitter"

    override fun apiDocs() = "https://apps.twitter.com/"

    override fun accountsLink() = "https://apps.twitter.com/"

    override fun parseCredentialsString(s: String): TwitterCredentials {
        val list = Splitter.on(",").splitToList(s)

        if (list.size != 5) {
            throw IllegalStateException("can't split '$s'")
        }

        return TwitterCredentials(list[0], list[1], list[2], list[3], list[4])
    }

    override fun formatCredentialsString(credentials: TwitterCredentials) = StringJoiner(",")
            .add(credentials.username)
            .add(credentials.consumerKey)
            .add(credentials.consumerSecret)
            .add(credentials.token)
            .add(credentials.secret)
            .toString()
}
