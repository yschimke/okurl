package com.baulsupp.oksocial.services.microsoft;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.SimpleWebServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.OutputHandler;
import java.io.IOException;
import java.util.Map;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MicrosoftAuthFlow {
  public static Oauth2Token login(OkHttpClient client, OutputHandler outputHandler, String clientId,
      String clientSecret) throws IOException {
    try (SimpleWebServer<String> s = SimpleWebServer.forCode()) {

      String loginUrl = "https://login.microsoftonline.com/common/oauth2/authorize"
          + "?client_id=" + clientId
          + "&response_type=code"
          + "&redirect_uri=" + s.getRedirectUri();

      outputHandler.openLink(loginUrl);

      String code = s.waitForCode();

      HttpUrl url = HttpUrl.parse("https://login.microsoftonline.com/common/oauth2/token");

      RequestBody body = new FormBody.Builder().add("grant_type", "authorization_code")
          .add("redirect_uri", s.getRedirectUri())
          .add("client_id", clientId)
          .add("client_secret", clientSecret)
          .add("code", code)
          .add("resource", "https://graph.microsoft.com/")
          .build();

      Request request = new Request.Builder().url(url).post(body).build();

      Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

      return new Oauth2Token((String) responseMap.get("access_token"),
          (String) responseMap.get("refresh_token"), clientId, clientSecret);
    }
  }
}
