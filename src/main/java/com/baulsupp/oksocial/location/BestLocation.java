package com.baulsupp.oksocial.location;

import java.io.IOException;
import java.util.Optional;

public class BestLocation implements LocationSource {
  @Override public Optional<Location> read() throws IOException {
    FileLocationSource fls = new FileLocationSource(FileLocationSource.FILE);

    Optional<Location> result = fls.read();

    if (!result.isPresent()) {
      CoreLocationCLI coreLoc = new CoreLocationCLI();
      result = coreLoc.read();
    }

    return result;
  }
}
