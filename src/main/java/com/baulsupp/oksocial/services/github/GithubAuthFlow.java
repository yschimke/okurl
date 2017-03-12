package com.baulsupp.oksocial.services.github;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.SimpleWebServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import ee.schimke.oksocial.output.OutputHandler;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static java.util.stream.Collectors.joining;

public class GithubAuthFlow {
  public static Oauth2Token login(OkHttpClient client, OutputHandler outputHandler, String clientId,
      String clientSecret, Set<String> scopes) throws IOException {
    try (SimpleWebServer<String> s = SimpleWebServer.forCode()) {
      String scopesString =
          URLEncoder.encode(scopes.stream().collect(joining(" ")), "UTF-8");

      String loginUrl = "https://github.com/login/oauth/authorize"
          + "?client_id=" + clientId
          + "&scope=" + scopesString
          + "&redirect_uri=" + s.getRedirectUri();

      outputHandler.openLink(loginUrl);

      String code = s.waitForCode();

      RequestBody body =
          new FormBody.Builder().add("client_id", clientId)
              .add("client_id", clientId)
              .add("code", code)
              .add("client_secret", clientSecret)
              .add("redirect_uri", s.getRedirectUri())
              .build();
      Request request =
          new Request.Builder().url("https://github.com/login/oauth/access_token")
              .header("Accept", "application/json")
              .post(body)
              .build();

      Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

      return new Oauth2Token((String) responseMap.get("access_token"));
    }
  }
}
