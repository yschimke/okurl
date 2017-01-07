package com.baulsupp.oksocial.services.linkedin;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.SimpleWebServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.OutputHandler;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static java.util.stream.Collectors.joining;

public class LinkedinAuthFlow {
  public static Oauth2Token login(OkHttpClient client, OutputHandler outputHandler, String clientId,
      String clientSecret, Set<String> scopes) throws IOException {
    try (SimpleWebServer<String> s = SimpleWebServer.forCode()) {

      String uuid = UUID.randomUUID().toString();

      String loginUrl = "https://www.linkedin.com/oauth/v2/authorization"
          + "?client_id=" + clientId
          + "&response_type=code"
          + "&redirect_uri=" + s.getRedirectUri()
          + "&state=" + uuid
          + "&scope=" + scopes.stream().collect(joining("+"));

      outputHandler.openLink(loginUrl);

      String code = s.waitForCode();

      String tokenUrl = "https://www.linkedin.com/oauth/v2/accessToken";
      RequestBody body =
          new FormBody.Builder().add("client_id", clientId)
              .add("redirect_uri", s.getRedirectUri())
              .add("client_secret", clientSecret)
              .add("code", code)
              .add("grant_type", "authorization_code")
              .build();
      Request request = new Request.Builder().url(tokenUrl).method("POST", body).build();

      Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

      return new Oauth2Token((String) responseMap.get("access_token"));
    }
  }
}
