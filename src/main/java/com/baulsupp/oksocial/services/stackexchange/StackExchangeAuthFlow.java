package com.baulsupp.oksocial.services.stackexchange;

import com.baulsupp.oksocial.authenticator.SimpleWebServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.ConsoleHandler;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Set;
import java.util.stream.Collectors;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.baulsupp.oksocial.authenticator.AuthUtil.makeSimpleRequest;

public class StackExchangeAuthFlow {

  public static Oauth2Token login(OkHttpClient client, String clientId,
      String clientSecret, Set<String> scopes) throws IOException {
    SimpleWebServer s = new SimpleWebServer();

    String serverUri = s.getRedirectUri();

    String loginUrl = "https://stackexchange.com/oauth"
        + "?client_id=" + clientId
        + "&redirect_uri=" + serverUri
        + "&scope=" + URLEncoder.encode(scopes.stream().collect(Collectors.joining(",")),
        "UTF-8");

    ConsoleHandler.openLink(loginUrl);

    String code = s.waitForCode();

    String tokenUrl = "https://stackexchange.com/oauth/access_token";

    RequestBody body =
        new FormBody.Builder().add("client_id", clientId).add("redirect_uri", serverUri)
            .add("client_secret", clientSecret).add("code", code).build();
    Request request = new Request.Builder().url(tokenUrl).method("POST", body).build();

    String longTokenBody = makeSimpleRequest(client, request);

    return new Oauth2Token(parseExchangeRequest(longTokenBody));
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
