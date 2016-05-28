package com.baulsupp.oksocial.authenticator;

import java.util.Optional;

public class ValidatedCredentials {
  public final Optional<String> username;
  public final Optional<String> clientName;

  public ValidatedCredentials(Optional<String> username, Optional<String> clientName) {
    this.username = username;
    this.clientName = clientName;
  }

  public ValidatedCredentials(String username, String client) {
    this(Optional.ofNullable(username), Optional.ofNullable(client));
  }
}
