package com.baulsupp.oksocial.services.facebook;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.SimpleWebServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.OutputHandler;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import okhttp3.OkHttpClient;

import static com.baulsupp.oksocial.authenticator.AuthUtil.makeSimpleGetRequest;

public class FacebookAuthFlow {

  public static Oauth2Token login(OkHttpClient client, OutputHandler outputHandler, String clientId,
      String clientSecret,
      Set<String> scopes) throws IOException {
    try (SimpleWebServer<String> s = SimpleWebServer.forCode()) {

      String serverUri = s.getRedirectUri();

      String loginUrl = "https://www.facebook.com/dialog/oauth"
          + "?client_id=" + clientId
          + "&redirect_uri=" + serverUri
          + "&scope=" + URLEncoder.encode(scopes.stream().collect(Collectors.joining(",")),
          "UTF-8");

      outputHandler.openLink(loginUrl);

      String code = s.waitForCode();

      String tokenUrl = "https://graph.facebook.com/v2.3/oauth/access_token"
          + "?client_id=" + clientId
          + "&redirect_uri=" + serverUri
          + "&client_secret=" + clientSecret
          + "&code=" + code;

      Map<String, Object> map =
          AuthUtil.makeJsonMapRequest(client, AuthUtil.uriGetRequest(tokenUrl));

      String shortToken = (String) map.get("access_token");

      String exchangeUrl = "https://graph.facebook.com/oauth/access_token"
          + "?grant_type=fb_exchange_token"
          + "&client_id=" + clientId
          + "&client_secret=" + clientSecret
          + "&fb_exchange_token=" + shortToken;

      String longTokenBody = makeSimpleGetRequest(client, exchangeUrl);

      return new Oauth2Token(parseExchangeRequest(longTokenBody));
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
}
