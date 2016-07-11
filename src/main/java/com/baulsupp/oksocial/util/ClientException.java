package com.baulsupp.oksocial.util;

import java.io.IOException;

public class ClientException extends IOException {
  private final String responseMessage;
  private final int code;

  public ClientException(String responseMessage, int code) {
    super("" + code + ": " + responseMessage);
    this.responseMessage = responseMessage;
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public String getResponseMessage() {
    return responseMessage;
  }
}
