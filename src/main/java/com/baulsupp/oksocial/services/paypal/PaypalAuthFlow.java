package com.baulsupp.oksocial.services.paypal;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import ee.schimke.oksocial.output.OutputHandler;
import java.io.IOException;
import java.util.Map;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class PaypalAuthFlow {
  public static Oauth2Token login(OkHttpClient client, String host, OutputHandler outputHandler,
      String clientId, String clientSecret) throws IOException {
    RequestBody body = new FormBody.Builder().add("grant_type", "client_credentials").build();

    String basic = Credentials.basic(clientId, clientSecret);
    Request request =
        new Request.Builder().url("https://" + host + "/v1/oauth2/token")
            .post(body)
            .header("Authorization", basic)
            .header("Accept", "application/json")
            .build();

    Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

    return new Oauth2Token((String) responseMap.get("access_token"));
  }
}
