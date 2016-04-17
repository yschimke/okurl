package com.baulsupp.oksocial.twitter;

import com.baulsupp.oksocial.commands.SimpleCommand;

public class Twitterapi extends SimpleCommand {
  public Twitterapi() {
    super("twitterapi", "https://api.twitter.com", TwitterAuthInterceptor.NAME);
  }
}

