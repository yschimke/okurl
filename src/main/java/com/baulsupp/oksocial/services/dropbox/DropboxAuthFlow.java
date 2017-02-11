package com.baulsupp.oksocial.services.dropbox;

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

public class DropboxAuthFlow {
  public static Oauth2Token login(OkHttpClient client, OutputHandler outputHandler, String clientId,
      String clientSecret) throws IOException {
    try (SimpleWebServer<String> s = SimpleWebServer.forCode()) {

      String loginUrl = "https://www.dropbox.com/1/oauth2/authorize"
          + "?client_id=" + clientId
          + "&response_type=code"
          + "&redirect_uri=" + s.getRedirectUri();

      outputHandler.openLink(loginUrl);

      String code = s.waitForCode();

      String basic = Credentials.basic(clientId, clientSecret);
      FormBody body = new FormBody.Builder().add("code", code)
          .add("grant_type", "authorization_code")
          .add("redirect_uri", s.getRedirectUri())
          .build();
      Request request =
          new Request.Builder().url("https://api.dropboxapi.com/1/oauth2/token")
              .post(body)
              .header("Authorization", basic)
              .build();

      Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

      return new Oauth2Token((String) responseMap.get("access_token"));
    }
  }
}
