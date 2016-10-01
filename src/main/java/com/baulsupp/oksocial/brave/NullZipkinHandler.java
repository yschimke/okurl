package com.baulsupp.oksocial.brave;

public class NullZipkinHandler extends ZipkinHandler {
  public static NullZipkinHandler instance() {
    return new NullZipkinHandler();
  }
}
