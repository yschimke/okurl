package com.baulsupp.oksocial.authenticator.oauth2

import java.util.Optional

class Oauth2Token {
    var accessToken: String
    var refreshToken: Optional<String>
    var clientId: Optional<String>
    var clientSecret: Optional<String>

    constructor(accessToken: String) {
        this.accessToken = accessToken
        this.refreshToken = Optional.empty()
    }

    constructor(accessToken: String, refreshToken: String, clientId: String,
                clientSecret: String) {
        this.accessToken = accessToken
        this.refreshToken = Optional.ofNullable(refreshToken)
        this.clientId = Optional.ofNullable(clientId)
        this.clientSecret = Optional.ofNullable(clientSecret)
    }
}
