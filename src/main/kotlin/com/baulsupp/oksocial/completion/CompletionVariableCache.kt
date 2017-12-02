package com.baulsupp.oksocial.completion

interface CompletionVariableCache {

  operator fun get(service: String, key: String): List<String>?

  fun store(service: String, key: String, values: List<String>)

  suspend fun compute(service: String, key: String,
              s: suspend () -> List<String>): List<String> {
    val values = get(service, key)

    return if (values != null) {
      values.toList()
    } else {
      val result = s()
      store(service, key, result)
      result
    }
  }

  companion object {
    val NONE: CompletionVariableCache = object : CompletionVariableCache {
      override fun get(service: String, key: String): List<String>? {
        return null
      }

      override fun store(service: String, key: String, values: List<String>) {}
    }
  }
}
