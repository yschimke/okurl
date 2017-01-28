package com.baulsupp.oksocial.services.twitter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TwitterAuthInterceptorTest {
  TwitterAuthInterceptor auth = new TwitterAuthInterceptor();

  @Test public void testHosts() {
    assertTrue(auth.hosts().contains("api.twitter.com"));
  }
}
