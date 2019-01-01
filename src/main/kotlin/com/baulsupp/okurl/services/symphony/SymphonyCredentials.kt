package com.baulsupp.okurl.services.symphony

data class SymphonyCredentials(
  val pod: String,
  val keystore: String,
  val password: String,
  val sessionToken: String?,
  val keyToken: String?
) {
  fun format(): String {
    return "$pod:$keystore:$password:${sessionToken.orEmpty()}:${keyToken.orEmpty()}"
  }

  companion object {
    fun parse(s: String): SymphonyCredentials {
      val (pod, keystore, password, authToken, keyToken) = s.split(":", limit = 5)
      return SymphonyCredentials(
        pod,
        keystore,
        password,
        if (authToken.isEmpty()) null else authToken,
        if (keyToken.isEmpty()) null else keyToken
      )
    }
  }
}
