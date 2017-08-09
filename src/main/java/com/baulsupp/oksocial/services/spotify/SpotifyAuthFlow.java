package com.baulsupp.oksocial.services.spotify;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.SimpleWebServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.OutputHandler;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static java.util.stream.Collectors.joining;

public class SpotifyAuthFlow {
  public static Oauth2Token login(OkHttpClient client, OutputHandler outputHandler, String clientId,
      String clientSecret, Set<String> scopes) throws IOException {
    try (SimpleWebServer<String> s = SimpleWebServer.forCode()) {

      String scopesString =
          URLEncoder.encode(scopes.stream().collect(joining(" ")), "UTF-8");

      String loginUrl = "https://accounts.spotify.com/authorize"
          + "?client_id=" + clientId
          + "&response_type=code"
          + "&state=x"
          + "&redirect_uri=" + s.getRedirectUri()
          + "&scope=" + scopesString;

      outputHandler.openLink(loginUrl);

      String code = s.waitForCode();

      String tokenUrl = "https://accounts.spotify.com/api/token";
      RequestBody body =
          new FormBody.Builder().add("client_id", clientId)
              .add("redirect_uri", s.getRedirectUri())
              .add("code", code)
              .add("grant_type", "authorization_code")
              .build();
      Request request = new Request.Builder().header("Authorization",
          Credentials.basic(clientId, clientSecret))
          .url(tokenUrl)
          .method("POST", body)
          .build();

      Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

      return new Oauth2Token((String) responseMap.get("access_token"),
          (String) responseMap.get("refresh_token"), clientId, clientSecret);
    }
  }
}
