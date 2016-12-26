package com.baulsupp.oksocial.services.lyft;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import java.io.IOException;
import java.util.Map;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class LyftClientAuthFlow {
  public static Oauth2Token login(OkHttpClient client, String clientId, String clientSecret)
      throws IOException {
    RequestBody body = RequestBody.create(MediaType.parse("application/json"),
        "{\"grant_type\": \"client_credentials\", \"scope\": \"public\"}");
    String basic = Credentials.basic(clientId, clientSecret);
    Request request =
        new Request.Builder().url("https://api.lyft.com/oauth/token")
            .post(body)
            .header("Authorization", basic)
            .build();

    Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

    // TODO refreshable without refresh token
    return new Oauth2Token((String) responseMap.get("access_token"));
  }
}
