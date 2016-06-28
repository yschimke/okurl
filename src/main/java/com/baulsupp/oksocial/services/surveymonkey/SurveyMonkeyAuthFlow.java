package com.baulsupp.oksocial.services.surveymonkey;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.SimpleWebServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.OutputHandler;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static java.util.stream.Collectors.joining;

public class SurveyMonkeyAuthFlow {
  public static Oauth2Token login(OkHttpClient client, OutputHandler outputHandler, String clientId,
      String apiKey,
      String secret, Set<String> scopes) throws IOException {
    try (SimpleWebServer<String> s = SimpleWebServer.forCode()) {

      String scopesString =
          URLEncoder.encode(scopes.stream().collect(joining(" ")), "UTF-8");

      String redirectUri = s.getRedirectUri();

      String loginUrl = "https://api.surveymonkey.net/oauth/authorize"
          + "?response_type=code"
          + "&client_id=" + clientId
          + "&api_key=" + apiKey
          + "&redirect_uri=" + redirectUri;

      outputHandler.openLink(loginUrl);

      String code = s.waitForCode();

      RequestBody body = new FormBody.Builder().add("client_secret", secret)
          .add("code", code)
          .add("redirect_uri", redirectUri)
          .add("grant_type", "authorization_code")
          .build();

      Request request =
          new Request.Builder().url("https://api.surveymonkey.net/oauth/token")
              .post(body)
              .build();

      Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

      return new Oauth2Token((String) responseMap.get("access_token"));
    }
  }
}
