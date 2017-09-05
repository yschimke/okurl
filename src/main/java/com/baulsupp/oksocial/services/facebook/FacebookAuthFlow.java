package com.baulsupp.oksocial.services.facebook;

import com.baulsupp.oksocial.authenticator.SimpleWebServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.OutputHandler;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import okhttp3.OkHttpClient;

import static com.baulsupp.oksocial.authenticator.AuthUtil.makeJsonMapRequest;
import static com.baulsupp.oksocial.authenticator.AuthUtil.uriGetRequest;
import static java.util.stream.Collectors.joining;

public class FacebookAuthFlow {

  public static Oauth2Token login(OkHttpClient client, OutputHandler outputHandler, String clientId,
      String clientSecret,
      Set<String> scopes) throws IOException {
    try (SimpleWebServer<String> s = SimpleWebServer.forCode()) {

      String serverUri = s.getRedirectUri();

      String loginUrl = "https://www.facebook.com/dialog/oauth"
          + "?client_id=" + clientId
          + "&redirect_uri=" + serverUri
          + "&scope=" + URLEncoder.encode(scopes.stream().collect(joining(",")),
          "UTF-8");

      outputHandler.openLink(loginUrl);

      String code = s.waitForCode();

      String tokenUrl = "https://graph.facebook.com/v2.10/oauth/access_token"
          + "?client_id=" + clientId
          + "&redirect_uri=" + serverUri
          + "&client_secret=" + clientSecret
          + "&code=" + code;

      Map<String, Object> map =
          makeJsonMapRequest(client, uriGetRequest(tokenUrl));

      String shortToken = (String) map.get("access_token");

      String exchangeUrl = "https://graph.facebook.com/oauth/access_token"
          + "?grant_type=fb_exchange_token"
          + "&client_id=" + clientId
          + "&client_secret=" + clientSecret
          + "&fb_exchange_token=" + shortToken;

      Map<String, Object> longTokenBody = makeJsonMapRequest(client, uriGetRequest(exchangeUrl));

      return new Oauth2Token((String) longTokenBody.get("access_token"));
    }
  }
}
