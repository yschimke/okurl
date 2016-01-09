package com.baulsupp.oksocial.twitter;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class PinAuthorisationFlow {
  public static void authorise(OkHttpClient client) throws IOException {
    RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "oauth_callback=oob");
    Request request = new Request.Builder().url("https://api.twitter.com/oauth/request_token").post(body).build();

    Response response = client.newCall(request).execute();

    System.out.println(response.body().source().readUtf8());
  }

  public static void main(String[] args) throws IOException {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();

    TwitterCredentials credentials = new TwitterCredentials("yschimke", "pKrYKZjbhN7rmtWXenRgr8kHY", "FpOK8mUesjggvZ7YprMnhStKmdyVcikNYtjNm1PetymgfE32jJ", null, "");

    builder.networkInterceptors().add(new TwitterAuthInterceptor(credentials));
    OkHttpClient client = builder.build();

    authorise(client);
  }
}
