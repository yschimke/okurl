package com.baulsupp.okurl.i9n

import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.okurl.Main
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompletionTest {
  private val main = Main()
  private val output = TestOutputHandler<Response>()
  private val credentialsStore = TestCredentialsStore()
  private val completionCache = TestCompletionVariableCache()

  @BeforeEach
  fun setup() {
    main.debug = true
    main.outputHandler = output
    main.credentialsStore = credentialsStore
    main.completionVariableCache = completionCache
    main.completionFile = File.createTempFile("okurltest", ".txt")
    main.urlComplete = true

    main.completionFile!!.deleteOnExit()
  }

  @Test
  fun completeEmpty() {
    main.arguments = mutableListOf("")

    runBlocking {
      main.run()
    }

    assertEquals(1, output.stdout.size)
    assertTrue(output.stdout[0].contains("https://api1.test.com/"))

    val cacheFileContent = readCompletionFile()
    assertEquals("[^/]*:?/?/?[^/]*", cacheFileContent[0])
  }

  @Test
  fun completeSingleEndpoint() {
    main.arguments = mutableListOf("https://api1.test.co")

    runBlocking {
      main.run()
    }

    assertEquals(listOf(
      "https://api1.test.com\nhttps://api1.test.com/"),
      output.stdout)

    val cacheFileContent = readCompletionFile()
    assertEquals("[^/]*:?/?/?[^/]*", cacheFileContent[0])
  }

  @Test
  fun completeEndpointsForTwitterApi() {
    main.arguments = mutableListOf("https://api.twitter.com/")

    runBlocking {
      main.run()
    }

    assertEquals(1, output.stdout.size)
    assertTrue(output.stdout[0].contains("\nhttps://api.twitter.com/1.1/geo/places.json\n"))

    val cacheFileContent = readCompletionFile()
    assertEquals("https://api.twitter.com/.*", cacheFileContent[0])
    assertTrue(cacheFileContent.contains("https://api.twitter.com/1.1/geo/places.json"))
  }

  private fun readCompletionFile(): List<String> = main.completionFile!!.readLines()
}
