package com.baulsupp.oksocial.services.twitter

import com.baulsupp.oksocial.services.twitter.joauth.Signature
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.junit.jupiter.api.Test

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.test.assertEquals

class SignatureTest {
  @Test
  fun testInitialRequestAuth() {
    val clock = Clock.fixed(Instant.ofEpochMilli(1460432867000L), ZoneId.of("UTC"))
    val s = Signature(clock) { 9L }

    val body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
        "oauth_callback=oob")
    val request = Request.Builder().url("https://api.twitter.com/oauth/request_token").post(
        body).build()

    val credentials = TwitterCredentials(null, "xxxxxxxxxxxxxxxxxxxxxxxxx",
        "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", null, "")

    val header = s.generateAuthorization(request, credentials)

    assertEquals(
        "OAuth oauth_consumer_key=\"xxxxxxxxxxxxxxxxxxxxxxxxx\", "
            + "oauth_nonce=\"91460432867000\", "
            + "oauth_signature=\"cL%2Fk1hU9rUo7%2FTIF%2BAwL5bbWhOE%3D\", "
            + "oauth_signature_method=\"HMAC-SHA1\", "
            + "oauth_timestamp=\"1460432867\", oauth_version=\"1.0\"",
        header)
  }
}
