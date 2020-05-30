package com.baulsupp.okurl

import org.junit.Test
import picocli.CommandLine
import kotlin.test.assertEquals

class MainTest {
  @Test
  fun testHeaders() {
    val main: Main = build("-H a:A -H b:B")

    assertEquals(mutableListOf("a:A", "b:B"), main.headers)
  }

  fun build(string: String): Main {
    return CommandLine.populateCommand(Main(), *string.split(" ").toTypedArray())
  }
}
