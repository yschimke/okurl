package com.baulsupp.okurl.kotlin

import kotlinx.coroutines.runBlocking

object ScriptMainTest {
  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      com.baulsupp.okurl.kotlin.main(arrayOf("./src/test/kotlin/commands/contributors.kts"))
    }
  }
}
