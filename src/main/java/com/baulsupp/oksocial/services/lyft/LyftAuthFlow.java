package com.baulsupp.oksocial.services.lyft;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.SimpleWebServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import ee.schimke.oksocial.output.OutputHandler;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static java.util.stream.Collectors.joining;

public class LyftAuthFlow {
  public static Oauth2Token login(OkHttpClient client, OutputHandler outputHandler, String clientId,
      String clientSecret, Set<String> scopes) throws IOException {
    try (SimpleWebServer<String> s = SimpleWebServer.forCode()) {

      String scopesString =
          URLEncoder.encode(scopes.stream().collect(joining(" ")), "UTF-8");

      String loginUrl = "https://api.lyft.com/oauth/authorize"
          + "?client_id=" + clientId
          + "&response_type=code"
          + "&scope=" + scopesString
          + "&state=x";

      outputHandler.openLink(loginUrl);

      String code = s.waitForCode();

      RequestBody body = RequestBody.create(MediaType.parse("application/json"),
          "{\"grant_type\": \"authorization_code\", \"code\": \"" + code + "\"}");
      String basic = Credentials.basic(clientId, clientSecret);
      Request request =
          new Request.Builder().url("https://api.lyft.com/oauth/token")
              .post(body)
              .header("Authorization", basic)
              .build();

      Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

      return new Oauth2Token((String) responseMap.get("access_token"),
          (String) responseMap.get("refresh_token"), clientId, clientSecret);
    }
  }
}
