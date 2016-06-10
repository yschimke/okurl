package com.baulsupp.oksocial.services.imgur;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.SimpleWebServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.ConsoleHandler;
import java.io.IOException;
import java.util.Map;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ImgurAuthFlow {
  public static Oauth2Token login(OkHttpClient client, String clientId,
      String clientSecret) throws IOException {
    SimpleWebServer s = new SimpleWebServer();

    String loginUrl = "https://api.imgur.com/oauth2/authorize"
        + "?client_id=" + clientId
        + "&response_type=code"
        + "&state=x";

    ConsoleHandler.openLink(loginUrl);

    String code = s.waitForCode();

    RequestBody body =
        new FormBody.Builder().add("client_id", clientId)
            .add("client_secret", clientSecret)
            .add("code", code)
            .add("grant_type", "authorization_code")
            .build();
    Request request = new Request.Builder().url("https://api.imgur.com/oauth2/token")
        .method("POST", body)
        .build();

    Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

    return new Oauth2Token((String) responseMap.get("access_token"),
        (String) responseMap.get("refresh_token"));
  }
}
