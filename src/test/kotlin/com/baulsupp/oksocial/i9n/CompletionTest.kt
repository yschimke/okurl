package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.oksocial.util.TestUtil.projectFile
import kotlinx.coroutines.experimental.runBlocking
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
    main.completionFile = File.createTempFile("oksocialtest", ".txt")
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
  fun completeEndpointShortCommand1() {
    main.commandName = "okapi"
    main.arguments = mutableListOf(projectFile("src/test/resources/commands/testcommand").absolutePath, "")

    runBlocking {
      main.run()
    }

    assertEquals(listOf("/account.json\n/users.json\n/usersList.json"), output.stdout)

    val cacheFileContent = readCompletionFile()
    assertEquals("/.*", cacheFileContent[0])
  }

  @Test
  fun completeEndpointShortCommand2() {
    main.commandName = "okapi"
    main.arguments = mutableListOf(projectFile("src/test/resources/commands/testcommand").absolutePath, "/users")

    runBlocking {
      main.run()
    }

    assertEquals(listOf("/users.json\n/usersList.json"), output.stdout)

    val cacheFileContent = readCompletionFile()
    assertEquals("/users.*", cacheFileContent[0])
  }

  @Test
  fun completeEndpointsForTwitter() {
    main.commandName = "okapi"
    main.arguments = mutableListOf(projectFile("src/test/kotlin/commands/twitterapi").absolutePath, "/")

    runBlocking {
      main.run()
    }

    assertEquals(1, output.stdout.size)
    assertTrue(output.stdout[0].contains("\n/1.1/geo/places.json\n"))

    val cacheFileContent = readCompletionFile()
    assertEquals("/.*", cacheFileContent[0])
    assertTrue(cacheFileContent.contains("/1.1/geo/places.json"))
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

  // //requires connection
  // @Test public void completeEndpointsForFacebook() throws Throwable {
  //  main.credentialsStore = new OSXCredentialsStore();
  //  main.urlCompletion = "/me";
  //  main.commandName = "okapi";
  //  main.arguments = listOf("commands/fbapi");
  //
  //  main.run();
  //
  //  assertEquals(0, output.failures.size());
  //  assertEquals(1, output.stdout.size());
  //  System.out.println(output.stdout.get(0));
  //
  //  List<String> cacheFileContent = readCompletionFile();
  //  assertEquals("/me", cacheFileContent.get(0));
  //  assertTrue(cacheFileContent.contains("/me"));
  //  assertTrue(cacheFileContent.contains("/me/videos"));
  // }
  //
  // //requires connection
  // @Test public void completeEndpointsForFacebookTopLevel() throws Throwable {
  //  main.credentialsStore = new OSXCredentialsStore();
  //  main.urlCompletion = "/";
  //  main.commandName = "okapi";
  //  main.arguments = listOf("commands/fbapi");
  //
  //  main.run();
  //
  //  if (output.failures.size() > 0) {
  //    output.failures.get(0).printStackTrace();
  //  }
  //
  //  assertEquals(0, output.failures.size());
  //  assertEquals(1, output.stdout.size());
  //
  //  List<String> cacheFileContent = readCompletionFile();
  //  assertEquals("/", cacheFileContent.get(0));
  //  assertTrue(cacheFileContent.contains("/v2.8"));
  //  assertTrue(cacheFileContent.contains("/me"));
  // }

  private fun readCompletionFile(): List<String> = main.completionFile!!.readLines()
}
