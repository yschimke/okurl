package com.baulsupp.oksocial.services.twitter

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.IOException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class SignatureTest {
    @Test
    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class, IOException::class)
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
