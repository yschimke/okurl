package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.oksocial.util.TestUtil.assumeHasNetwork
import com.google.common.collect.Lists.newArrayList
import okhttp3.OkHttpClient
import okhttp3.Response
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException

class DiscoveryApiDocPresenterTest {
  private val outputHandler = TestOutputHandler<Response>()
  private val client = OkHttpClient()

  private var p: DiscoveryApiDocPresenter? = null

  @BeforeEach
  @Throws(IOException::class)
  fun loadPresenter() {
    val discoveryIndex = DiscoveryIndex.loadStatic()
    p = DiscoveryApiDocPresenter(discoveryIndex)
  }

  @Test
  @Throws(IOException::class)
  fun testExplainsUrl() {
    assumeHasNetwork()

    p!!.explainApi("https://people.googleapis.com/v1/{+resourceName}", outputHandler, client)

    val es = newArrayList("name: Google People API",
        "docs: https://developers.google.com/people/",
        "url: https://people.googleapis.com/v1/{+resourceName}"
    )

    // unsafe tests
    // "endpoint id: people.people.get",
    //"scopes: https://www.googleapis.com/auth/contacts, https://www.googleapis.com/auth/contacts.readonly, https://www.googleapis.com/auth/plus.login, https://www.googleapis.com/auth/user.addresses.read, https://www.googleapis.com/auth/user.birthday.read, https://www.googleapis.com/auth/user.emails.read, https://www.googleapis.com/auth/user.phonenumbers.read, https://www.googleapis.com/auth/userinfo.email, https://www.googleapis.com/auth/userinfo.profile"

    for (l in es) {
      assertTrue(outputHandler.stdout.contains(l), l)
    }

    //assertTrue(outputHandler.stdout.stream()
    //    .anyMatch(c -> c.startsWith("Provides information about a person")));
  }

  @Test
  @Throws(IOException::class)
  fun testExplainsExpandedUrl() {
    assertMatch("https://people.googleapis.com/v1/people/me",
        "https://people.googleapis.com/v1/{+resourceName}", "url")
  }

  @Test
  @Throws(IOException::class)
  fun testExplainsExpandedUrl2() {
    assertMatch("https://people.googleapis.com/v1/people:batchGet?resourceNames=me",
        "https://people.googleapis.com/v1/people:batchGet", "url")
  }

  @Test
  @Throws(IOException::class)
  fun testExplainsExpandedUrl3() {
    assertMatch("https://www.googleapis.com/tasks/v1/users/@me/lists",
        "https://www.googleapis.com/tasks/v1/users/@me/lists", "url")
  }

  @Test
  @Throws(IOException::class)
  fun testExplainsExpandedUrl4() {
    assertMatch("https://www.googleapis.com/tasks/v1/users/@me/lists/x",
        "https://www.googleapis.com/tasks/v1/users/@me/lists/{tasklist}", "url")
  }

  @Test
  @Throws(IOException::class)
  fun testExplainsExpandedWWWBeforeSiteUrls() {
    assertMatch("https://www.googleapis.com/tasks/v1/",
        "https://developers.google.com/google-apps/tasks/firstapp", "docs")
  }

  @Test
  @Throws(IOException::class)
  fun testExplainsExpandedWWWAfterSiteUrls() {
    assertMatch("https://www.googleapis.com/tasks/v1/users/@me/lists",
        "https://developers.google.com/google-apps/tasks/firstapp", "docs")
  }

  @Throws(IOException::class)
  private fun assertMatch(requested: String, expected: String, field: String) {
    assumeHasNetwork()

    p!!.explainApi(requested, outputHandler, client)

    val contains = outputHandler.stdout.contains(field + ": " + expected)

    if (!contains) {
      val found = outputHandler.stdout.stream().filter { s ->
        s.startsWith(field + ": ")
      }.findFirst()

      fail<Any>("expected '$expected' found " + found.map { s -> s.substring(field.length + 2) }
          .orElse("nothing"))
    }

    assertTrue(contains)
  }
}
