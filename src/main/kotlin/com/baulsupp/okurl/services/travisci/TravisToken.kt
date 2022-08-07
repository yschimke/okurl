package com.baulsupp.okurl.services.travisci

data class TravisToken(val token: String? = null) {
  companion object {
    fun external(): TravisToken = TravisToken()
  }
}
