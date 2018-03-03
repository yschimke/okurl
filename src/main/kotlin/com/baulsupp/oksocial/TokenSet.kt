package com.baulsupp.oksocial

sealed class Token

val DefaultToken = TokenSet("default")

data class TokenSet(val name: String) : Token()
data class TokenValue(val token: Any) : Token()
object NoToken : Token()
