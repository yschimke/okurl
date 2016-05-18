package com.baulsupp.oksocial.lyft;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LyftAuthFlow {
  public static LyftServerCredentials login(OkHttpClient client) throws IOException {
    String clientId = System.console().readLine("Lyft Client Id: ");
    String clientSecret = new String(System.console().readPassword("Lyft Client Secret: "));

    RequestBody body = RequestBody.create(MediaType.parse("application/json"),
        "{\"grant_type\": \"client_credentials\", \"scope\": \"public\"}");
    String basic = Credentials.basic(clientId, clientSecret);
    Request request =
        new Request.Builder().url("https://api.lyft.com/oauth/token")
            .post(body)
            .header("Authorization", basic)
            .build();

    Response response = client.newCall(request).execute();

    try {
      if (response.code() != 200) {
        throw new IOException("Failed auth " + response.code() + response.message());
      } else {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map =
            mapper.readValue(response.body().string(), new TypeReference<Map<String, String>>() {
            });

        return new LyftServerCredentials(map.get("access_token"));
      }
    } finally {
      response.body().close();
    }
  }
}
