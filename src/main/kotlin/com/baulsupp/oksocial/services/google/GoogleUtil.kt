package com.baulsupp.oksocial.services.google

import java.util.Arrays

object GoogleUtil {
  val SCOPES: Collection<String> = Arrays.asList("plus.login", "plus.profile.emails.read")

  val API_HOSTS = setOf((
      "api.google.com")
  )

  fun fullScope(suffix: String): String {
    return if (suffix.contains("/")) suffix else "https://www.googleapis.com/auth/" + suffix
  }
}
