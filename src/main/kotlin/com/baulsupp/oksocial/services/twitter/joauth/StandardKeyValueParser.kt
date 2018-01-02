package com.baulsupp.oksocial.services.twitter.joauth

/**
 * StandardKeyValueParser is a KeyValueParser that splits a string on a delimiter,
 * and then splits each pair with the kvDelimiter. both delimiters can be java-style
 * regular expressions.
 */
class StandardKeyValueParser(private val delimiter: String, private val kvDelimiter: String) {
  fun parse(input: String, handlers: List<KeyValueHandler>) {
    if (empty(input)) return

    val tokens = input.split(delimiter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

    tokens
      .map { token -> token.split(kvDelimiter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() }
      .forEach {
        when (it.size) {
          2 -> if (!empty(it[0])) {
            for (handler in handlers) handler.handle(it[0], it[1])
          }
          1 -> if (!empty(it[0])) {
            for (handler in handlers) handler.handle(it[0], "")
          }
          else -> {
          }
        }
      }
  }

  private fun empty(str: String?): Boolean {
    return str == null || str.isEmpty()
  }
}