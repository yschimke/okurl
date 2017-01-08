package com.baulsupp.oksocial.services.google;

import java.io.IOException;
import org.junit.Test;

public class DiscoveryRegistryTest {
  @Test public void loadStatic() throws IOException {
    DiscoveryRegistry r = DiscoveryRegistry.loadStatic();
  }
}