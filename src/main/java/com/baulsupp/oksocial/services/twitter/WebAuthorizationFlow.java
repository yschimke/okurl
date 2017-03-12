package com.baulsupp.oksocial.services.twitter;

import com.baulsupp.oksocial.authenticator.SimpleWebServer;
import ee.schimke.oksocial.output.OutputHandler;
import java.io.IOException;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import okhttp3.OkHttpClient;

public class WebAuthorizationFlow extends TwitterAuthFlow {
  public WebAuthorizationFlow(OkHttpClient client, OutputHandler outputHandler) {
    super(client, outputHandler);
  }

  public TwitterCredentials authorise(String consumerKey, String consumerSecret)
      throws IOException {
    Function<HttpServletRequest, String> codeReader = r -> r.getParameter("oauth_verifier");

    try (SimpleWebServer<String> s = new SimpleWebServer(codeReader)) {
      TwitterCredentials unauthed =
          new TwitterCredentials(null, consumerKey, consumerSecret, null, "");

      TwitterCredentials requestCredentials = generateRequestToken(unauthed, s.getRedirectUri());

      showUserLogin(requestCredentials);

      String verifier = s.waitForCode();

      return generateAccessToken(requestCredentials, verifier);
    }
  }
}
