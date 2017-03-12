package com.baulsupp.oksocial.services.twitter;

import ee.schimke.oksocial.output.OutputHandler;
import java.io.IOException;
import okhttp3.OkHttpClient;

public class PinAuthorizationFlow extends TwitterAuthFlow {
  public PinAuthorizationFlow(OkHttpClient client, OutputHandler outputHandler) {
    super(client, outputHandler);
  }

  protected String promptForPin(TwitterCredentials newCredentials)
      throws IOException {
    System.err.println(
        "Authorise by entering the PIN through a web browser");

    showUserLogin(newCredentials);

    return new String(System.console().readPassword("Enter PIN: "));
  }

  public TwitterCredentials authorise(String consumerKey, String consumerSecret)
      throws IOException {
    TwitterCredentials unauthed =
        new TwitterCredentials(null, consumerKey, consumerSecret, null, "");

    TwitterCredentials requestCredentials = generateRequestToken(unauthed, "oob");

    String pin = promptForPin(requestCredentials);

    return generateAccessToken(requestCredentials, pin);
  }
}
