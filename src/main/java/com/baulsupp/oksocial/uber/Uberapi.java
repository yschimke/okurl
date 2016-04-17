package com.baulsupp.oksocial.uber;

import com.baulsupp.oksocial.commands.SimpleCommand;

public class Uberapi extends SimpleCommand {
  public Uberapi() {
    super("uberapi", "https://api.uber.com", UberAuthInterceptor.NAME);
  }
}

