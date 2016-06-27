package com.baulsupp.oksocial.services.twitter;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TwitterAuthInterceptorTest {
  TwitterAuthInterceptor auth = new TwitterAuthInterceptor();

  @Test public void testHosts() {
    assertTrue(auth.hosts().contains("api.twitter.com"));
  }
}
