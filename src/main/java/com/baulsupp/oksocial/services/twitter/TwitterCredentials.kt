package com.baulsupp.oksocial.services.twitter

class TwitterCredentials {
    var username: String
    var consumerKey: String
    var consumerSecret: String
    var token: String
    var secret: String

    constructor() {}

    constructor(username: String, consumerKey: String, consumerSecret: String,
                token: String, secret: String) {
        this.username = username
        this.consumerKey = consumerKey
        this.consumerSecret = consumerSecret
        this.token = token
        this.secret = secret
    }

    override fun toString(): String {
        return "TwitterCredentials{"
        +"username='"
        +username
        +'\''
        +", consumerKey='"
        +consumerKey
        +'\''
        +
        ", consumerSecret='"
        +consumerSecret
        +'\''
        +", token='"
        +token
        +'\''
        +", secret='"
        +secret
        +'\''
        +'}'
    }
}
