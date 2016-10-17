package com.baulsupp.oksocial.location;

import java.io.IOException;
import java.util.Optional;

public interface LocationSource {
  Optional<Location> read() throws IOException;
}
