package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.SimpleWebServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.ConsoleHandler;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class GoogleAuthFlow {
  public static Oauth2Token login(OkHttpClient client, String clientId,
      String clientSecret, Set<String> scopes) throws IOException {
    SimpleWebServer s = new SimpleWebServer();

    String scopesString =
        URLEncoder.encode(scopes.stream().collect(Collectors.joining(" ")), "UTF-8");

    String redirectUri = s.getRedirectUri();

    String loginUrl = "https://accounts.google.com/o/oauth2/v2/auth"
        + "?client_id=" + URLEncoder.encode(clientId, "UTF-8")
        + "&response_type=code"
        + "&scope=" + scopesString
        + "&state=x"
        + "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8");

    ConsoleHandler.openLink(loginUrl);

    String code = s.waitForCode();

    String tokenUrl = "https://www.googleapis.com/oauth2/v4/token";
    RequestBody body =
        new FormBody.Builder().add("client_id", clientId)
            .add("redirect_uri", redirectUri)
            .add("client_secret", clientSecret)
            .add("code", code)
            .add("grant_type", "authorization_code")
            .build();
    Request request = new Request.Builder().url(tokenUrl).method("POST", body).build();

    Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

    return new Oauth2Token((String) responseMap.get("access_token"));
  }
}
