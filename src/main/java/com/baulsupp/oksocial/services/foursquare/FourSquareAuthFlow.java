package com.baulsupp.oksocial.services.foursquare;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.LocalServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.ConsoleHandler;
import com.baulsupp.oksocial.util.JsonUtil;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import okhttp3.OkHttpClient;

public class FourSquareAuthFlow {

  public static Oauth2Token login(OkHttpClient client, String clientId, String clientSecret)
      throws IOException {
    LocalServer s = new LocalServer("localhost", 3000);

    try {
      String serverUri = s.getRedirectUri();

      String loginUrl = "https://foursquare.com/oauth2/authenticate"
          + "?client_id=" + clientId
          + "&redirect_uri=" + URLEncoder.encode(serverUri, "UTF-8")
          + "&response_type=code";

      ConsoleHandler.openLink(loginUrl);

      String code = s.waitForCode();

      String tokenUrl = "https://foursquare.com/oauth2/access_token"
          + "?client_id=" + clientId
          + "&client_secret=" + clientSecret
          + "&grant_type=authorization_code"
          + "&redirect_uri=" + URLEncoder.encode(serverUri, "UTF-8")
          + "&code=" + code;

      String response = AuthUtil.makeSimpleGetRequest(client, tokenUrl);

      Map<String, Object> responseMap = JsonUtil.map(response);

      return new Oauth2Token((String) responseMap.get("access_token"));
    } finally {
      s.stop();
    }
  }
}
