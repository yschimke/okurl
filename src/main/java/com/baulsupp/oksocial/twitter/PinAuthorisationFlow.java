package com.baulsupp.oksocial.twitter;

import com.baulsupp.oksocial.ConsoleHandler;
import com.google.common.base.Throwables;
import com.twitter.joauth.keyvalue.KeyValueHandler;
import com.twitter.joauth.keyvalue.KeyValueParser;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PinAuthorisationFlow {
  private static final MediaType FORM_URL_ENCODED =
      MediaType.parse("application/x-www-form-urlencoded");

  public static TwitterCredentials authorise(OkHttpClient client, TwitterCredentials unauthed) {
    try {
      TwitterCredentials requestCredentials = generateRequestToken(client, unauthed);

      String pin = promptForPin(requestCredentials);

      return generateAccessToken(client, requestCredentials, pin);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  private static TwitterCredentials generateRequestToken(OkHttpClient client,
      TwitterCredentials unauthed) throws IOException {
    RequestBody body = RequestBody.create(FORM_URL_ENCODED, "oauth_callback=oob");
    Request request =
        new Request.Builder().url("https://api.twitter.com/oauth/request_token")
            .post(body)
            .build();

    request = request.newBuilder()
        .header("Authorization",
            new TwitterAuthInterceptor(unauthed).generateAuthorization(request))
        .build();

    Response response = client.newCall(request).execute();

    try {
      if (!response.isSuccessful()) {
        throw new IllegalStateException("unable to request token");
      }

      Map<String, String> tokenMap = parseTokenMap(response.body().source().readUtf8());
      return new TwitterCredentials(unauthed.username, unauthed.consumerKey,
          unauthed.consumerSecret,
          tokenMap.get("oauth_token"), tokenMap.get("oauth_token_secret"));
    } finally {
      response.body().close();
    }
  }

  private static String promptForPin(TwitterCredentials newCredentials) throws IOException {
    System.err.println(
        "Authorise by entering the PIN through a web browser");
    ConsoleHandler.openLink(
        "http://api.twitter.com/oauth/authenticate?oauth_token=" + newCredentials.token);

    return new String(System.console().readPassword("Enter PIN: "));
  }

  private static TwitterCredentials generateAccessToken(OkHttpClient client,
      TwitterCredentials requestCredentials, String pin) throws IOException {
    RequestBody body = RequestBody.create(FORM_URL_ENCODED, "oauth_verifier=" + pin);
    Request request =
        new Request.Builder().url("https://api.twitter.com/oauth/access_token")
            .post(body)
            .build();

    request = request.newBuilder()
        .header("Authorization",
            new TwitterAuthInterceptor(requestCredentials).generateAuthorization(request))
        .build();

    Response response = client.newCall(request).execute();

    try {
      if (!response.isSuccessful()) {
        throw new IllegalStateException("unable to authorize token");
      }

      String s = response.body().source().readUtf8();
      Map<String, String> tokenMap = parseTokenMap(s);
      return new TwitterCredentials(tokenMap.get("screen_name"), requestCredentials.consumerKey,
          requestCredentials.consumerSecret,
          tokenMap.get("oauth_token"), tokenMap.get("oauth_token_secret"));
    } finally {
      response.body().close();
    }
  }

  private static Map<String, String> parseTokenMap(String tokenDetails) {
    KeyValueHandler.SingleKeyValueHandler handler =
        new KeyValueHandler.SingleKeyValueHandler();

    KeyValueParser.StandardKeyValueParser bodyParser =
        new KeyValueParser.StandardKeyValueParser("&", "=");
    bodyParser.parse(tokenDetails, Collections.singletonList(handler));

    return handler.toMap();
  }
}
