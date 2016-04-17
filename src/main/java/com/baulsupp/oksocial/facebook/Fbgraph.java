package com.baulsupp.oksocial.facebook;

import com.baulsupp.oksocial.commands.SimpleCommand;

public class Fbgraph extends SimpleCommand {
  public Fbgraph() {
    super("fbgraph", "https://graph.facebook.com", FacebookAuthInterceptor.NAME);
  }
}
