package com.baulsupp.oksocial.location;

import com.baulsupp.oksocial.util.Util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * https://github.com/fulldecent/corelocationcli
 */
public class Location {
  public final double latitude;
  public final double longitude;

  public Location(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  @Override public String toString() {
    return latitude + "," + longitude;
  }

  public static Optional<Location> read() throws IOException {
    if (Util.isOSX()) {
      ProcessBuilder pb =
          new ProcessBuilder("/Applications/CoreLocationCLI", "-format", "%latitude,%longitude", "-once", "yes")
              .redirectInput(ProcessBuilder.Redirect.INHERIT)
              .redirectError(ProcessBuilder.Redirect.INHERIT);
      Process process = pb.start();

      try {
        OutputStream processStdin = process.getOutputStream();
        processStdin.close();

        try {
          boolean completed = process.waitFor(5, TimeUnit.SECONDS);

          if (!completed) {
            throw new IOException("/Applications/CoreLocationCLI failed to launch in 5 seconds");
          }

          int result = process.exitValue();
          if (result != 0) {
            System.err.println("/Applications/CoreLocationCLI returned " + result);
          }

          try (InputStream is = process.getInputStream()) {
            BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line = r.readLine();

            String[] parts = line.split(",");
            return Location.latLong(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
          }
        } catch (InterruptedException e) {
          throw new IOException(e);
        }
      } finally {
        if (process.isAlive()) {
          process.destroyForcibly();
        }
      }
    } else {
      return Optional.empty();
    }
  }

  private static Optional<Location> latLong(double latitude, double longitude) {
    Location l = new Location(latitude, longitude);

    return Optional.of(l);
  }

  public static void main(String[] args) throws IOException {
    System.out.println(Location.read());
  }
}
