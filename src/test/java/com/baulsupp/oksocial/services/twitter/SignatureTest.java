package com.baulsupp.oksocial.services.twitter;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SignatureTest {
  @Test public void testInitialRequestAuth()
      throws NoSuchAlgorithmException, InvalidKeyException, IOException {
    Clock clock = Clock.fixed(Instant.ofEpochMilli(1460432867000L), ZoneId.of("UTC"));
    Signature s = new Signature(clock, () -> 9L);

    RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
        "oauth_callback=oob");
    Request request =
        new Request.Builder().url("https://api.twitter.com/oauth/request_token").post(body).build();

    TwitterCredentials credentials = new TwitterCredentials(null, "xxxxxxxxxxxxxxxxxxxxxxxxx",
        "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", null, "");

    String header = s.generateAuthorization(request, credentials);

    assertEquals(
        "OAuth oauth_consumer_key=\"xxxxxxxxxxxxxxxxxxxxxxxxx\", "
            + "oauth_nonce=\"91460432867000\", "
            + "oauth_signature=\"cL%2Fk1hU9rUo7%2FTIF%2BAwL5bbWhOE%3D\", "
            + "oauth_signature_method=\"HMAC-SHA1\", "
            + "oauth_timestamp=\"1460432867\", oauth_version=\"1.0\"",
        header);
  }
}
