package com.baulsupp.oksocial.services.squareup;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.SimpleWebServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.util.JsonUtil;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SquareUpAuthFlow {

  public static Oauth2Token login(OkHttpClient client, OutputHandler outputHandler, String clientId,
      String clientSecret, Set<String> scopes) throws IOException {
    try (SimpleWebServer<String> s = SimpleWebServer.forCode()) {

      String serverUri = s.getRedirectUri();

      String loginUrl = "https://connect.squareup.com/oauth2/authorize"
          + "?client_id=" + clientId
          + "&redirect_uri=" + URLEncoder.encode(serverUri, "UTF-8")
          + "&response_type=code"
          + "&scope=" + URLEncoder.encode(scopes.stream().collect(Collectors.joining(" ")),
          "UTF-8");

      outputHandler.openLink(loginUrl);

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
      Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

      return new Oauth2Token((String) responseMap.get("access_token"));
    }
  }
}
