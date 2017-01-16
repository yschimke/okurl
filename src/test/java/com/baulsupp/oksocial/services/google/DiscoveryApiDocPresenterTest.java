package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.i9n.TestOutputHandler;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import okhttp3.OkHttpClient;
import org.junit.Before;
import org.junit.Test;

import static com.baulsupp.oksocial.util.TestUtil.assumeHasNetwork;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DiscoveryApiDocPresenterTest {
  private TestOutputHandler outputHandler = new TestOutputHandler();
  private OkHttpClient client = new OkHttpClient();

  private DiscoveryApiDocPresenter p;

  @Before public void loadPresenter() throws IOException {
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
        "Provides information about a person resource for a resource name. Use `people/me` to indicate the authenticated user.",
        "",
        "parameter: resourceName (string)",
        "The resource name of the person to provide information about. - To get information about the authenticated user, specify `people/me`. - To get information about any user, specify the resource name that identifies the user, such as the resource names returned by [`people.connections.list`](/people/api/rest/v1/people.connections/list).",
        "parameter: requestMask.includeField (string)",
        "Comma-separated list of fields to be included in the response. Omitting this field will include all fields. Each path should start with `person.`: for example, `person.names` or `person.photos`."
    );

    assertEquals(es, outputHandler.stdout);
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
