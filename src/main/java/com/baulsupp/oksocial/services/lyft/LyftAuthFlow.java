package com.baulsupp.oksocial.services.lyft;

import com.baulsupp.oksocial.authenticator.LocalServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.ConsoleHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LyftAuthFlow {
  public static Oauth2Token login(OkHttpClient client, String clientId,
      String clientSecret, Set<String> scopes) throws IOException {
    LocalServer s = new LocalServer("localhost", 3000);

    try {
      String scopesString = URLEncoder.encode(scopes.stream().collect(Collectors.joining(" ")), "UTF-8");

      String loginUrl = "https://api.lyft.com/oauth/authorize"
          + "?client_id=" + clientId
          + "&response_type=code"
          + "&scope=" + scopesString
          + "&state=x";

      ConsoleHandler.openLink(loginUrl);

      String code = s.waitForCode();

      RequestBody body = RequestBody.create(MediaType.parse("application/json"),
          "{\"grant_type\": \"authorization_code\", \"code\": \"" + code + "\"}");
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

          return new Oauth2Token(map.get("access_token"));
        }
      } finally {
        response.body().close();
      }
    } finally {
      s.stop();
    }
  }
}
