package com.baulsupp.oksocial.services.google

object GoogleUtil {
  val SCOPES = listOf("plus.login", "plus.profile.emails.read")

  val API_HOSTS = setOf((
    "api.google.com")
  )

  fun fullScope(suffix: String): String {
    return if (suffix.contains("/")) suffix else "https://www.googleapis.com/auth/$suffix"
  }
}
