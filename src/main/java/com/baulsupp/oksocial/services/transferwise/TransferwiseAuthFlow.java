package com.baulsupp.oksocial.services.transferwise;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.SimpleWebServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.OutputHandler;
import java.io.IOException;
import java.util.Map;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TransferwiseAuthFlow {
  public static Oauth2Token login(OkHttpClient client, OutputHandler outputHandler, String host,
      String clientId, String clientSecret) throws IOException {
    try (SimpleWebServer<String> s = SimpleWebServer.forCode()) {
      String serverUri = s.getRedirectUri();

      String loginUrl = "https://" + host + "/oauth/authorize"
          + "?client_id=" + clientId
          + "&response_type=code"
          + "&scope=transfers"
          + "&redirect_uri=" + serverUri;

      outputHandler.openLink(loginUrl);

      String code = s.waitForCode();

      RequestBody body =
          new FormBody.Builder().add("client_id", clientId).add("redirect_uri", serverUri)
              .add("grant_type", "authorization_code").add("code", code).build();
      String basic = Credentials.basic(clientId, clientSecret);
      Request request =
          new Request.Builder().url("https://" + host + "/oauth/token")
              .post(body)
              .header("Authorization", basic)
              .build();

      Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

      return new Oauth2Token((String) responseMap.get("access_token"),
          (String) responseMap.get("refresh_token"), clientId, clientSecret);
    }
  }
}
