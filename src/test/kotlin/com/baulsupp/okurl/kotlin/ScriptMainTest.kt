package com.baulsupp.okurl.kotlin

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking

object ScriptMainTest {
  @ExperimentalCoroutinesApi
  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      com.baulsupp.okurl.kotlin.main(arrayOf("./src/test/kotlin/commands/contributors.kts"))
    }
  }
}
