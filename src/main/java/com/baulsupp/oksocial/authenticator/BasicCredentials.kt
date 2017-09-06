package com.baulsupp.oksocial.authenticator

class BasicCredentials {
    var user: String
    var password: String

    constructor() {}

    constructor(user: String, password: String) {
        this.user = user
        this.password = password
    }
}
