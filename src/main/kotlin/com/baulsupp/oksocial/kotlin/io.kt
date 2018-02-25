package com.baulsupp.oksocial.kotlin

import java.io.Console

suspend fun Console.readPasswordString(prompt: String): String {
  return run {
    String(System.console().readPassword(prompt))
  }
}

suspend fun Console.readString(prompt: String): String {
  return run {
    System.console().readLine(prompt)
  }
}
