package com.baulsupp.oksocial.authenticator.oauth2

import com.baulsupp.oksocial.AbstractServiceDefinition

class Oauth2ServiceDefinition(apiHost: String, serviceName: String, shortName: String,
                              apiDocs: String, accountsLink: String?) : AbstractServiceDefinition<Oauth2Token>(apiHost, serviceName, shortName, apiDocs, accountsLink) {

    override fun parseCredentialsString(s: String): Oauth2Token {
        val parts = s.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        return if (parts.size < 4) {
            Oauth2Token(parts[0])
        } else {
            Oauth2Token(parts[0], parts[1], parts[2], parts[3])
        }
    }

    override fun formatCredentialsString(credentials: Oauth2Token): String {
        if (credentials.refreshToken != null
                && credentials.clientId != null
                && credentials.clientSecret != null) {
            return "${credentials.accessToken}:${credentials.refreshToken}:${credentials.clientId}:${credentials.clientSecret}"
        } else {
            return credentials.accessToken
        }
    }
}
