package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.output.TestOutputHandler
import com.google.common.collect.Lists
import com.google.common.collect.Lists.newArrayList
import okhttp3.Response
import org.junit.Before
import org.junit.Test


import java.io.File
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompletionTest {
    private val main = Main()
    private val output = TestOutputHandler<Response>()
    private val credentialsStore = TestCredentialsStore()
    private val completionCache = TestCompletionVariableCache()

    @Before
    @Throws(IOException::class)
    fun setup() {
        main.outputHandler = output
        main.credentialsStore = credentialsStore
        main.completionVariableCache = completionCache
        main.completionFile = File.createTempFile("oksocialtest", ".txt").path
        main.urlComplete = true

        File(main.completionFile).deleteOnExit()
    }

    @Test
    @Throws(Throwable::class)
    fun completeEmpty() {
        main.arguments = newArrayList("")

        main.run()

        assertEquals(1, output.stdout.size)
        assertTrue(output.stdout[0].contains("https://api1.test.com/"))

        val cacheFileContent = readCompletionFile()
        assertEquals("[^/]*:?/?/?[^/]*", cacheFileContent[0])
    }

    @Test
    @Throws(Throwable::class)
    fun completeSingleEndpoint() {
        main.arguments = newArrayList("https://api1.test.co")

        main.run()

        assertEquals(Lists.newArrayList(
                "https://api1.test.com\nhttps://api1.test.com/"),
                output.stdout)

        val cacheFileContent = readCompletionFile()
        assertEquals("[^/]*:?/?/?[^/]*", cacheFileContent[0])
    }

    @Test
    @Throws(Throwable::class)
    fun completeEndpointShortCommand1() {
        main.commandName = "okapi"
        main.arguments = Lists.newArrayList("src/test/resources/commands/testcommand", "")

        main.run()

        assertEquals(Lists.newArrayList("/account.json\n/users.json\n/usersList.json"), output.stdout)

        val cacheFileContent = readCompletionFile()
        assertEquals("/.*", cacheFileContent[0])
    }

    @Test
    @Throws(Throwable::class)
    fun completeEndpointShortCommand2() {
        main.commandName = "okapi"
        main.arguments = Lists.newArrayList("src/test/resources/commands/testcommand", "/users")

        main.run()

        assertEquals(Lists.newArrayList("/users.json\n/usersList.json"), output.stdout)

        val cacheFileContent = readCompletionFile()
        assertEquals("/users.*", cacheFileContent[0])
    }

    @Test
    @Throws(Throwable::class)
    fun completeEndpointsForTwitter() {
        main.commandName = "okapi"
        main.arguments = Lists.newArrayList("commands/twitterapi", "/")

        main.run()

        assertEquals(1, output.stdout.size)
        assertTrue(output.stdout[0].contains("\n/1.1/geo/places.json\n"))

        val cacheFileContent = readCompletionFile()
        assertEquals("/.*", cacheFileContent[0])
        assertTrue(cacheFileContent.contains("/1.1/geo/places.json"))
    }

    @Test
    @Throws(Throwable::class)
    fun completeEndpointsForTwitterApi() {
        main.arguments = newArrayList("https://api.twitter.com/")

        main.run()

        assertEquals(1, output.stdout.size)
        assertTrue(output.stdout[0].contains("\nhttps://api.twitter.com/1.1/geo/places.json\n"))

        val cacheFileContent = readCompletionFile()
        assertEquals("https://api.twitter.com/.*", cacheFileContent[0])
        assertTrue(cacheFileContent.contains("https://api.twitter.com/1.1/geo/places.json"))
    }

    ////requires connection
    //@Test public void completeEndpointsForFacebook() throws Throwable {
    //  main.credentialsStore = new OSXCredentialsStore();
    //  main.urlCompletion = "/me";
    //  main.commandName = "okapi";
    //  main.arguments = Lists.newArrayList("commands/fbapi");
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
    //}
    //
    ////requires connection
    //@Test public void completeEndpointsForFacebookTopLevel() throws Throwable {
    //  main.credentialsStore = new OSXCredentialsStore();
    //  main.urlCompletion = "/";
    //  main.commandName = "okapi";
    //  main.arguments = Lists.newArrayList("commands/fbapi");
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
    //}

    @Throws(IOException::class)
    private fun readCompletionFile(): List<String> {
        return Files.readAllLines(FileSystems.getDefault().getPath(main.completionFile))
    }
}
