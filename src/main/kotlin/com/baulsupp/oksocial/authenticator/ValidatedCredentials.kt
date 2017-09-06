package com.baulsupp.oksocial.authenticator

import java.util.Optional

class ValidatedCredentials(val username: Optional<String>, val clientName: Optional<String>) {

    constructor(username: String, client: String) : this(Optional.ofNullable<String>(username), Optional.ofNullable<String>(client)) {}
}
