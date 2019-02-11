package com.baulsupp.okurl.authenticator.authflow

sealed class AuthOption<T> {
  abstract val param: String
}

data class Prompt(
  override val param: String,
  val label: String,
  val default: String? = null,
  val secret: Boolean = false
) :
  AuthOption<String>()

data class Scopes(
  override val param: String,
  val label: String,
  val default: List<String>? = null,
  val known: List<String>
) :
  AuthOption<List<String>>()

object Callback: AuthOption<String>() {
  override val param = "callback"
}

object State: AuthOption<String>() {
  override val param = "state"
}
