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
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class GoogleAuthFlow {
  public static Oauth2Token login(OkHttpClient client, String clientId,
      String clientSecret, Set<String> scopes) throws IOException {
    SimpleWebServer s = new SimpleWebServer();

    String scopesString =
        URLEncoder.encode(scopes.stream().collect(Collectors.joining(" ")), "UTF-8");

    String loginUrl = "https://api.google.com/oauth/authorize"
        + "?client_id=" + clientId
        + "&response_type=code"
        + "&scope=" + scopesString
        + "&state=x";

    ConsoleHandler.openLink(loginUrl);

    String code = s.waitForCode();

    RequestBody body = RequestBody.create(MediaType.parse("application/json"),
        "{\"grant_type\": \"authorization_code\", \"code\": \"" + code + "\"}");
    String basic = Credentials.basic(clientId, clientSecret);
    Request request =
        new Request.Builder().url("https://api.google.com/oauth/token")
            .post(body)
            .header("Authorization", basic)
            .build();

    Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

    return new Oauth2Token((String) responseMap.get("access_token"));
  }
}
