package com.baulsupp.oksocial.authenticator.oauth2

class Oauth2Token {
    var accessToken: String
    var refreshToken: String? = null
    var clientId: String? = null
    var clientSecret: String? = null

    constructor(accessToken: String) {
        this.accessToken = accessToken
    }

    constructor(accessToken: String, refreshToken: String?, clientId: String?,
                clientSecret: String?) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.clientId = clientId
        this.clientSecret = clientSecret
    }

    fun isRenewable(): Boolean = refreshToken != null && clientId != null && clientSecret != null
}
