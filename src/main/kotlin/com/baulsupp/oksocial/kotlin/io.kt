package com.baulsupp.oksocial.kotlin

import kotlinx.coroutines.experimental.async
import java.io.Console

suspend fun Console.readPasswordString(prompt: String): String {
  return async {
    String(System.console().readPassword(prompt))
  }.await()
}

suspend fun Console.readString(prompt: String): String {
  return async {
    System.console().readLine(prompt)
  }.await()
}
