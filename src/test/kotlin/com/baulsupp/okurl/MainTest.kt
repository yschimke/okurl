package com.baulsupp.okurl

import com.github.rvesse.airline.SingleCommand
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class MainTest {
  @Test
  fun testHeaders() {
    val main: Main = build("-H a:A -H b:B")

    assertEquals(mutableListOf("a:A", "b:B"), main.headers)
  }

  @Test
  fun testCerts() {
    val main: Main = build("--cert a.crt --cert b.crt --cert c.crt")

    assertEquals(mutableListOf(File("a.crt"), File("b.crt"), File("c.crt")), main.serverCerts)
  }

  private fun build(string: String): Main {
    val args = string.split(" ")

    return SingleCommand.singleCommand(Main::class.java).parse(*args.toTypedArray())
  }
}
