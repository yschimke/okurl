package com.baulsupp.oksocial.services.stackexchange;

import com.baulsupp.oksocial.authenticator.SimpleWebServer;
import ee.schimke.oksocial.output.OutputHandler;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Set;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.baulsupp.oksocial.authenticator.AuthUtil.makeSimpleRequest;
import static java.util.stream.Collectors.joining;

public class StackExchangeAuthFlow {

  public static StackExchangeToken login(OkHttpClient client, OutputHandler outputHandler,
      String clientId, String clientSecret, String clientKey, Set<String> scopes)
      throws IOException {
    try (SimpleWebServer<String> s = SimpleWebServer.forCode()) {

      String serverUri = s.getRedirectUri();

      String loginUrl = "https://stackexchange.com/oauth"
          + "?client_id=" + clientId
          + "&redirect_uri=" + serverUri
          + "&scope=" + URLEncoder.encode(scopes.stream().collect(joining(",")),
          "UTF-8");

      outputHandler.openLink(loginUrl);

      String code = s.waitForCode();

      String tokenUrl = "https://stackexchange.com/oauth/access_token";

      RequestBody body =
          new FormBody.Builder().add("client_id", clientId).add("redirect_uri", serverUri)
              .add("client_secret", clientSecret).add("code", code).build();
      Request request = new Request.Builder().url(tokenUrl).method("POST", body).build();

      String longTokenBody = makeSimpleRequest(client, request);

      return new StackExchangeToken(parseExchangeRequest(longTokenBody), clientKey);
    }
  }

  private static String parseExchangeRequest(String body) {
    String[] params = body.split("&");

    for (String p : params) {
      String[] parts = p.split("=");

      if (parts[0].equals("access_token")) {
        return parts[1];
      }
    }

    return null;
  }
}
