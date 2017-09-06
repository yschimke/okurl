package com.baulsupp.oksocial.services.stackexchange

import com.baulsupp.oksocial.credentials.ServiceDefinition
import java.util.Optional

import java.util.Optional.of

class StackExchangeServiceDefinition : ServiceDefinition<StackExchangeToken> {
    override fun apiHost(): String {
        return "api.stackexchange.com"
    }

    override fun serviceName(): String {
        return "StackExchange API"
    }

    override fun shortName(): String {
        return "stackexchange"
    }

    override fun parseCredentialsString(s: String): StackExchangeToken {
        val parts = s.split(":".toRegex(), 2).toTypedArray()
        return StackExchangeToken(parts[0], parts[1])
    }

    override fun formatCredentialsString(credentials: StackExchangeToken): String {
        return credentials.accessToken + ":" + credentials.key
    }

    override fun apiDocs(): Optional<String> {
        return of("https://api.stackexchange.com/docs")
    }

    override fun accountsLink(): Optional<String> {
        return of("http://stackapps.com/apps/oauth")
    }
}
