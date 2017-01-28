package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.Main;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompletionTest {
  private Main main = new Main();
  private TestOutputHandler output = new TestOutputHandler();
  private TestCredentialsStore credentialsStore = new TestCredentialsStore();
  private TestCompletionVariableCache completionCache = new TestCompletionVariableCache();

  @BeforeEach
  public void setup() throws IOException {
    main.outputHandler = output;
    main.credentialsStore = credentialsStore;
    main.completionVariableCache = completionCache;
    main.completionFile = File.createTempFile("oksocialtest", ".txt").getPath();
    main.urlComplete = true;

    new File(main.completionFile).deleteOnExit();
  }

  @Test public void completeEmpty() throws Throwable {
    main.arguments = newArrayList("");

    main.run();

    assertEquals(1, output.stdout.size());
    assertTrue(output.stdout.get(0).contains("https://api1.test.com/"));

    List<String> cacheFileContent = readCompletionFile();
    assertEquals("[^/]*:?/?/?[^/]*", cacheFileContent.get(0));
  }

  @Test public void completeSingleEndpoint() throws Throwable {
    main.arguments = newArrayList("https://api1.test.co");

    main.run();

    assertEquals(Lists.newArrayList(
        "https://api1.test.com\nhttps://api1.test.com/"),
        output.stdout);

    List<String> cacheFileContent = readCompletionFile();
    assertEquals("[^/]*:?/?/?[^/]*", cacheFileContent.get(0));
  }

  @Test public void completeEndpointShortCommand1() throws Throwable {
    main.commandName = "okapi";
    main.arguments = Lists.newArrayList("src/test/resources/commands/testcommand", "");

    main.run();

    assertEquals(Lists.newArrayList("/account.json\n/users.json\n/usersList.json"), output.stdout);

    List<String> cacheFileContent = readCompletionFile();
    assertEquals("/.*", cacheFileContent.get(0));
  }

  @Test public void completeEndpointShortCommand2() throws Throwable {
    main.commandName = "okapi";
    main.arguments = Lists.newArrayList("src/test/resources/commands/testcommand", "/users");

    main.run();

    assertEquals(Lists.newArrayList("/users.json\n/usersList.json"), output.stdout);

    List<String> cacheFileContent = readCompletionFile();
    assertEquals("/users.*", cacheFileContent.get(0));
  }

  @Test public void completeEndpointsForTwitter() throws Throwable {
    main.commandName = "okapi";
    main.arguments = Lists.newArrayList("commands/twitterapi", "/");

    main.run();

    assertEquals(1, output.stdout.size());
    assertTrue(output.stdout.get(0).contains("\n/1.1/geo/places.json\n"));

    List<String> cacheFileContent = readCompletionFile();
    assertEquals("/.*", cacheFileContent.get(0));
    assertTrue(cacheFileContent.contains("/1.1/geo/places.json"));
  }

  @Test public void completeEndpointsForTwitterApi() throws Throwable {
    main.arguments = newArrayList("https://api.twitter.com/");

    main.run();

    assertEquals(1, output.stdout.size());
    assertTrue(output.stdout.get(0).contains("\nhttps://api.twitter.com/1.1/geo/places.json\n"));

    List<String> cacheFileContent = readCompletionFile();
    assertEquals("https://api.twitter.com/.*", cacheFileContent.get(0));
    assertTrue(cacheFileContent.contains("https://api.twitter.com/1.1/geo/places.json"));
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

  private List<String> readCompletionFile() throws IOException {
    return Files.readAllLines(FileSystems.getDefault().getPath(main.completionFile));
  }
}
