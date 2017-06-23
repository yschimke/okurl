package com.baulsupp.oksocial.services.slack;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.SimpleWebServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.OutputHandler;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static java.util.stream.Collectors.joining;

public class SlackAuthFlow {
  public static Oauth2Token login(OkHttpClient client, OutputHandler outputHandler, String clientId,
      String clientSecret, Set<String> scopes) throws IOException {
    try (SimpleWebServer<String> s = SimpleWebServer.forCode()) {

      String scopesString =
          URLEncoder.encode(scopes.stream().collect(joining(" ")), "UTF-8");

      String loginUrl = "https://slack.com/oauth/authorize"
          + "?client_id=" + clientId
          + "&redirect_uri=" + s.getRedirectUri()
          + "&scope=" + scopesString;

      outputHandler.openLink(loginUrl);

      String code = s.waitForCode();

      HttpUrl url = HttpUrl.parse("https://api.slack.com/api/oauth.access")
          .newBuilder()
          .addQueryParameter("client_id", clientId)
          .addQueryParameter("client_secret", clientSecret)
          .addQueryParameter("redirect_uri", s.getRedirectUri())
          .addQueryParameter("code", code)
          .build();

      Request request = new Request.Builder().url(url).build();

      Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

      if (!responseMap.get("ok").equals(true)) {
        throw new IOException("authorization failed: " + responseMap.get("error"));
      }

      return new Oauth2Token((String) responseMap.get("access_token"));
    }
  }
}
