package com.baulsupp.oksocial.authenticator;

public class BasicCredentials {
  public String user;
  public String password;

  public BasicCredentials() {
  }

  public BasicCredentials(String user, String password) {
    this.user = user;
    this.password = password;
  }

  @Override
  public String toString() {
    return "BasicCredentials{"
        + "user='" + user + '\''
        + "password='" + password + '\''
        + '}';
  }
}
