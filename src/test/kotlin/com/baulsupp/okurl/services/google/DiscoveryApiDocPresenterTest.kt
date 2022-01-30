package com.baulsupp.okurl.services.google

import com.baulsupp.schoutput.handler.TestOutputHandler
import com.baulsupp.okurl.credentials.DefaultToken
import com.baulsupp.okurl.util.TestUtil.assumeHasNetwork
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Response
import org.junit.Ignore
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.fail

@Disabled
class DiscoveryApiDocPresenterTest {
  private val outputHandler = TestOutputHandler<Response>()
  private val client = OkHttpClient()

  private var p: DiscoveryApiDocPresenter? = DiscoveryApiDocPresenter(DiscoveryRegistry(client))

  @Test
  fun testExplainsUrl() {
    assumeHasNetwork()

    runBlocking {
      p!!.explainApi("https://people.googleapis.com/v1/{+resourceName}", outputHandler, client,
        DefaultToken)
    }

    val es = listOf("name: People API",
      "docs: https://developers.google.com/people/",
      "url: https://people.googleapis.com/v1/{+resourceName}"
    )

    // unsafe tests
    // "endpoint id: people.people.get",
    // "scopes: https://www.googleapis.com/auth/contacts, https://www.googleapis.com/auth/contacts.readonly, https://www.googleapis.com/auth/plus.login, https://www.googleapis.com/auth/user.addresses.read, https://www.googleapis.com/auth/user.birthday.read, https://www.googleapis.com/auth/user.emails.read, https://www.googleapis.com/auth/user.phonenumbers.read, https://www.googleapis.com/auth/userinfo.email, https://www.googleapis.com/auth/userinfo.profile"

    for (l in es) {
      assertTrue(outputHandler.stdout.contains(l), l)
    }

    // assertTrue(outputHandler.stdout.stream()
    //    .anyMatch(c -> c.startsWith("Provides information about a person")));
  }

  @Test
  fun testExplainsExpandedUrl() {
    assertMatch("https://people.googleapis.com/v1/people/me",
      "https://people.googleapis.com/v1/{+resourceName}", "url")
  }

  @Test
  fun testExplainsExpandedUrl2() {
    assertMatch("https://people.googleapis.com/v1/people:batchGet?resourceNames=me",
      "https://people.googleapis.com/v1/people:batchGet", "url")
  }

  @Test
  fun testExplainsExpandedUrl3() {
    assertMatch("https://www.googleapis.com/tasks/v1/users/@me/lists",
      "https://www.googleapis.com/tasks/v1/users/@me/lists", "url")
  }

  @Test
  fun testExplainsExpandedUrl4() {
    assertMatch("https://www.googleapis.com/tasks/v1/users/@me/lists/x",
      "https://www.googleapis.com/tasks/v1/users/@me/lists/{tasklist}", "url")
  }

  @Test
  fun testExplainsExpandedWWWBeforeSiteUrls() {
    assertMatch("https://www.googleapis.com/tasks/v1/",
      "https://developers.google.com/google-apps/tasks/firstapp", "docs")
  }

  @Test
  fun testExplainsExpandedWWWAfterSiteUrls() {
    assertMatch("https://www.googleapis.com/tasks/v1/users/@me/lists",
      "https://developers.google.com/google-apps/tasks/firstapp", "docs")
  }

  private fun assertMatch(requested: String, expected: String, field: String) {
    assumeHasNetwork()

    runBlocking {
      p!!.explainApi(requested, outputHandler, client,
        DefaultToken)
    }

    val contains = outputHandler.stdout.contains("$field: $expected")

    if (!contains) {
      val found = outputHandler.stdout.firstOrNull { s ->
        s.startsWith("$field: ")
      }

      fail("expected '$expected' found " + (found?.substring(field.length + 2) ?: "nothing"))
    }

    assertTrue(contains)
  }
}
