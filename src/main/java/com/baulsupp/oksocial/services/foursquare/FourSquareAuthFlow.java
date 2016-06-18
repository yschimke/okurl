package com.baulsupp.oksocial.services.foursquare;

import com.baulsupp.oksocial.authenticator.SimpleWebServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.ConsoleHandler;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import okhttp3.OkHttpClient;

import static com.baulsupp.oksocial.authenticator.AuthUtil.makeJsonMapRequest;
import static com.baulsupp.oksocial.authenticator.AuthUtil.uriGetRequest;

public class FourSquareAuthFlow {

  public static Oauth2Token login(OkHttpClient client, String clientId, String clientSecret)
      throws IOException {
    try (SimpleWebServer<String> s = SimpleWebServer.forCode()) {

      String serverUri = s.getRedirectUri();

      String loginUrl = "https://foursquare.com/oauth2/authenticate"
          + "?client_id=" + clientId
          + "&redirect_uri=" + URLEncoder.encode(serverUri, "UTF-8")
          + "&response_type=code";

      new ConsoleHandler(false).openLink(loginUrl);

      String code = s.waitForCode();

      String tokenUrl = "https://foursquare.com/oauth2/access_token"
          + "?client_id=" + clientId
          + "&client_secret=" + clientSecret
          + "&grant_type=authorization_code"
          + "&redirect_uri=" + URLEncoder.encode(serverUri, "UTF-8")
          + "&code=" + code;

      Map<String, Object> responseMap =
          makeJsonMapRequest(client, uriGetRequest(tokenUrl));

      return new Oauth2Token((String) responseMap.get("access_token"));
    }
  }
}
