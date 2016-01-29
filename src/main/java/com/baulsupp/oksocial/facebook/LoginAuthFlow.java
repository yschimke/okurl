package com.baulsupp.oksocial.facebook;

import com.baulsupp.oksocial.ConsoleHandler;
import com.baulsupp.oksocial.LocalServer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginAuthFlow {
  public static final String CLIENT_ID = "550830111748623";
  public static final String CLIENT_SECRET = "";

  public static FacebookCredentials login(OkHttpClient client) throws IOException {
    LocalServer s = new LocalServer("localhost", 3000);

    try {
      String serverUri = s.getRedirectUri();

      String loginUrl = "https://www.facebook.com/dialog/oauth?client_id="
          + CLIENT_ID
          + "&redirect_uri="
          + serverUri;

      ConsoleHandler.openLink(loginUrl);

      String code = s.waitForCode();

      String tokenUrl = "https://graph.facebook.com/v2.3/oauth/access_token"
          + "?client_id=" + CLIENT_ID
          + "&redirect_uri=" + serverUri
          + "&client_secret=" + CLIENT_SECRET
          + "&code=" + code;

      String shortTokenJson = makeRequest(client, tokenUrl);

      ObjectMapper mapper = new ObjectMapper();
      JsonNode tree = mapper.readTree(shortTokenJson);

      String shortToken = tree.get("access_token").asText();

      String exchangeUrl = "https://graph.facebook.com/oauth/access_token"
          + "?grant_type=fb_exchange_token"
          + "&client_id=" + CLIENT_ID
          + "&client_secret=" + CLIENT_SECRET
          + "&fb_exchange_token=" + shortToken;

      String longTokenBody = makeRequest(client, exchangeUrl);

      return new FacebookCredentials(parseExchangeRequest(longTokenBody));
    } finally {
      s.stop();
    }
  }

  private static String makeRequest(OkHttpClient client, String exchangeUri) throws IOException {
    Request request = new Request.Builder().url(exchangeUri).build();
    Response response = client.newCall(request).execute();

    try {
      if (!response.isSuccessful()) {
        throw new IllegalStateException("unable to request token");
      }

      String body = response.body().source().readString(Charsets.UTF_8);

      //System.out.println(body);

      return body;
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

  public static void main(String[] args) throws IOException {
    LoginAuthFlow.login(new OkHttpClient());
  }
}
