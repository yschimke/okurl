package com.baulsupp.oksocial.services.twitter;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.twitter.joauth.keyvalue.KeyValueHandler;
import com.twitter.joauth.keyvalue.KeyValueParser;
import com.baulsupp.oksocial.output.OutputHandler;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public abstract class TwitterAuthFlow {
  protected final OkHttpClient client;
  protected final OutputHandler outputHandler;

  public TwitterAuthFlow(OkHttpClient client, OutputHandler outputHandler) {

    this.client = client;
    this.outputHandler = outputHandler;
  }

  protected TwitterCredentials generateRequestToken(TwitterCredentials unauthed, String callback)
      throws IOException {
    RequestBody body = new FormBody.Builder().add("oauth_callback", callback).build();
    Request request =
        new Request.Builder().url("https://api.twitter.com/oauth/request_token")
            .post(body)
            .build();

    request = request.newBuilder()
        .header("Authorization",
            new Signature().generateAuthorization(request, unauthed))
        .build();

    String response = AuthUtil.makeSimpleRequest(client, request);

    Map<String, String> tokenMap = parseTokenMap(response);
    return new TwitterCredentials(unauthed.username, unauthed.consumerKey,
        unauthed.consumerSecret,
        tokenMap.get("oauth_token"), tokenMap.get("oauth_token_secret"));
  }

  protected TwitterCredentials generateAccessToken(TwitterCredentials requestCredentials,
      String verifier) throws IOException {
    RequestBody body = new FormBody.Builder().add("oauth_verifier", verifier).build();
    Request request =
        new Request.Builder().url("https://api.twitter.com/oauth/access_token")
            .post(body)
            .build();

    request = request.newBuilder()
        .header("Authorization",
            new Signature().generateAuthorization(request, requestCredentials))
        .build();

    String response = AuthUtil.makeSimpleRequest(client, request);

    Map<String, String> tokenMap = parseTokenMap(response);

    return new TwitterCredentials(tokenMap.get("screen_name"), requestCredentials.consumerKey,
        requestCredentials.consumerSecret,
        tokenMap.get("oauth_token"), tokenMap.get("oauth_token_secret"));
  }

  protected static Map<String, String> parseTokenMap(String tokenDetails) {
    KeyValueHandler.SingleKeyValueHandler handler =
        new KeyValueHandler.SingleKeyValueHandler();

    KeyValueParser.StandardKeyValueParser bodyParser =
        new KeyValueParser.StandardKeyValueParser("&", "=");
    bodyParser.parse(tokenDetails, Collections.singletonList(handler));

    return handler.toMap();
  }

  protected void showUserLogin(TwitterCredentials newCredentials) throws IOException {
    outputHandler.openLink(
        "https://api.twitter.com/oauth/authenticate?oauth_token=" + newCredentials.token);
  }
}
