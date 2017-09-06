package com.baulsupp.oksocial.services.stackexchange

class StackExchangeToken {
    var accessToken: String
    var key: String

    constructor() {}

    constructor(accessToken: String, key: String) {
        this.accessToken = accessToken
        this.key = key
    }
}
