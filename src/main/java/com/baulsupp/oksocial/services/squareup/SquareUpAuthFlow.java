package com.baulsupp.oksocial.services.squareup;

import com.baulsupp.oksocial.output.ConsoleHandler;
import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.LocalServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.util.JsonUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SquareUpAuthFlow {

  public static Oauth2Token login(OkHttpClient client, String clientId, String clientSecret,
      Set<String> scopes)
      throws IOException {
    LocalServer s = new LocalServer("localhost", 3000);

    try {
      String serverUri = s.getRedirectUri();

      String loginUrl = "https://connect.squareup.com/oauth2/authorize"
          + "?client_id=" + clientId
          + "&redirect_uri=" + serverUri
          + "&response_type=code"
          + "&scope=" + scopes.stream().collect(Collectors.joining(" "));

      ConsoleHandler.openLink(loginUrl);

      String code = s.waitForCode();

      String tokenUrl = "https://connect.squareup.com/oauth2/token";
      Map<String, String> map = new HashMap<>();
      map.put("client_id", clientId);
      map.put("client_secret", clientSecret);
      map.put("code", code);
      map.put("redirect_uri", serverUri);
      String body = JsonUtil.toJson(map);

      RequestBody reqBody = RequestBody.create(MediaType.parse("application/json"), body);
      Request request = new Request.Builder().url(tokenUrl).post(reqBody).build();
      String response = AuthUtil.makeSimpleRequest(client, request);

      Map<String, Object> responseMap = JsonUtil.map(response);

      return new Oauth2Token((String) responseMap.get("access_token"));
    } finally {
      s.stop();
    }
  }
}
