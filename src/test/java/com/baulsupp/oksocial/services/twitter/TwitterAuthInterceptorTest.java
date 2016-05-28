package com.baulsupp.oksocial.services.twitter;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TwitterAuthInterceptorTest {
  class FixedTimeTwitterAuthInterceptor extends TwitterAuthInterceptor {
    public FixedTimeTwitterAuthInterceptor(
        TwitterCredentials credentials) {
      super(credentials);
    }

    @Override public long generateTimestamp() {
      return 1460432867L;
    }

    @Override public String generateNonce() {
      return "67822045175727268931460435281112";
    }
  }

  @Test
  public void testInitialRequestAuth()
      throws NoSuchAlgorithmException, InvalidKeyException, IOException {
    RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
        "oauth_callback=oob");
    Request request =
        new Request.Builder().url("https://api.twitter.com/oauth/request_token").post(body).build();

    TwitterCredentials credentials = new TwitterCredentials(null, "xxxxxxxxxxxxxxxxxxxxxxxxx",
        "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", null, "");

    String header =
        new FixedTimeTwitterAuthInterceptor(credentials).generateAuthorization(request);

    assertEquals(
        "OAuth oauth_consumer_key=\"xxxxxxxxxxxxxxxxxxxxxxxxx\", "
            + "oauth_nonce=\"67822045175727268931460435281112\", "
            + "oauth_signature=\"g6jUnrM1E6NldgYQugt%2Frh8Fq%2Fw%3D\", "
            + "oauth_signature_method=\"HMAC-SHA1\", "
            + "oauth_timestamp=\"1460432867\", oauth_version=\"1.0\"",
        header);
  }
}
