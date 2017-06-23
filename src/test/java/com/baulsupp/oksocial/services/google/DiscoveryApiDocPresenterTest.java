package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.output.TestOutputHandler;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.baulsupp.oksocial.util.TestUtil.assumeHasNetwork;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class DiscoveryApiDocPresenterTest {
  private TestOutputHandler<Response> outputHandler = new TestOutputHandler<Response>();
  private OkHttpClient client = new OkHttpClient();

  private DiscoveryApiDocPresenter p;

  @BeforeEach
  public void loadPresenter() throws IOException {
    DiscoveryIndex discoveryIndex = DiscoveryIndex.loadStatic();
    p = new DiscoveryApiDocPresenter(discoveryIndex);
  }

  @Test public void testExplainsUrl() throws IOException {
    assumeHasNetwork();

    p.explainApi("https://people.googleapis.com/v1/{+resourceName}", outputHandler, client);

    List<String> es = newArrayList("name: Google People API",
        "docs: https://developers.google.com/people/", "endpoint id: people.people.get",
        "url: https://people.googleapis.com/v1/{+resourceName}",
        "scopes: https://www.googleapis.com/auth/contacts, https://www.googleapis.com/auth/contacts.readonly, https://www.googleapis.com/auth/plus.login, https://www.googleapis.com/auth/user.addresses.read, https://www.googleapis.com/auth/user.birthday.read, https://www.googleapis.com/auth/user.emails.read, https://www.googleapis.com/auth/user.phonenumbers.read, https://www.googleapis.com/auth/userinfo.email, https://www.googleapis.com/auth/userinfo.profile",
        "",
        "Provides information about a person for a resource name. Use\n"
            + "`people/me` to indicate the authenticated user."
    );

    for (String l: es) {
      assertTrue(outputHandler.stdout.contains(l), l);
    }
  }

  @Test public void testExplainsExpandedUrl() throws IOException {
    assertMatch("https://people.googleapis.com/v1/people/me",
        "https://people.googleapis.com/v1/{+resourceName}", "url");
  }

  @Test public void testExplainsExpandedUrl2() throws IOException {
    assertMatch("https://people.googleapis.com/v1/people:batchGet?resourceNames=me",
        "https://people.googleapis.com/v1/people:batchGet", "url");
  }

  @Test public void testExplainsExpandedUrl3() throws IOException {
    assertMatch("https://www.googleapis.com/tasks/v1/users/@me/lists",
        "https://www.googleapis.com/tasks/v1/users/@me/lists", "url");
  }

  @Test public void testExplainsExpandedUrl4() throws IOException {
    assertMatch("https://www.googleapis.com/tasks/v1/users/@me/lists/x",
        "https://www.googleapis.com/tasks/v1/users/@me/lists/{tasklist}", "url");
  }

  @Test public void testExplainsExpandedWWWBeforeSiteUrls() throws IOException {
    assertMatch("https://www.googleapis.com/tasks/v1/",
        "https://developers.google.com/google-apps/tasks/firstapp", "docs");
  }

  @Test public void testExplainsExpandedWWWAfterSiteUrls() throws IOException {
    assertMatch("https://www.googleapis.com/tasks/v1/users/@me/lists",
        "https://developers.google.com/google-apps/tasks/firstapp", "docs");
  }

  private void assertMatch(String requested, String expected, String field) throws IOException {
    assumeHasNetwork();

    p.explainApi(requested, outputHandler, client);

    boolean contains = outputHandler.stdout.contains(field + ": " + expected);

    if (!contains) {
      Optional<String> found =
          outputHandler.stdout.stream().filter(s -> s.startsWith(field + ": ")).findFirst();

      fail("expected '" + expected + "' found " + found.map(s -> s.substring(field.length() + 2))
          .orElse("nothing"));
    }

    assertTrue(contains);
  }
}
