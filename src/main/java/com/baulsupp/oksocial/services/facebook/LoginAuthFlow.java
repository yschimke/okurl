package com.baulsupp.oksocial.services.facebook;

import com.baulsupp.oksocial.ConsoleHandler;
import com.baulsupp.oksocial.authenticator.LocalServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginAuthFlow {

  public static Oauth2Token login(OkHttpClient client, String clientId, String clientSecret,
      Set<String> scopes) {
    try {
      LocalServer s = new LocalServer("localhost", 3000);

      try {
        String serverUri = s.getRedirectUri();

        String loginUrl = "https://www.facebook.com/dialog/oauth"
            + "?client_id=" + clientId
            + "&redirect_uri=" + serverUri
            + "&scope=" + scopes.stream().collect(Collectors.joining(","));

        ConsoleHandler.openLink(loginUrl);

        String code = s.waitForCode();

        String tokenUrl = "https://graph.facebook.com/v2.3/oauth/access_token"
            + "?client_id=" + clientId
            + "&redirect_uri=" + serverUri
            + "&client_secret=" + clientSecret
            + "&code=" + code;

        String shortTokenJson = makeRequest(client, tokenUrl);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode tree = mapper.readTree(shortTokenJson);

        String shortToken = tree.get("access_token").asText();

        String exchangeUrl = "https://graph.facebook.com/oauth/access_token"
            + "?grant_type=fb_exchange_token"
            + "&client_id=" + clientId
            + "&client_secret=" + clientSecret
            + "&fb_exchange_token=" + shortToken;

        String longTokenBody = makeRequest(client, exchangeUrl);

        return new Oauth2Token(parseExchangeRequest(longTokenBody));
      } finally {
        s.stop();
      }
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  private static String makeRequest(OkHttpClient client, String exchangeUri) throws IOException {
    Request request = new Request.Builder().url(exchangeUri).build();
    Response response = client.newCall(request).execute();

    try {
      if (!response.isSuccessful()) {
        throw new IllegalStateException(
            "unable to request token " + response.code() + ": " + response.message());
      }

      return response.body().source().readString(Charsets.UTF_8);
    } finally {
      response.body().close();
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
