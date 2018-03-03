package com.baulsupp.oksocial

sealed class Token {
  open fun name(): String? = null
}

val DefaultToken = TokenSet("default")

data class TokenSet(val name: String) : Token() {
  override fun name(): String? = name
}

data class TokenValue(val token: Any) : Token()

object NoToken : Token()
