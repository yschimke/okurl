package com.baulsupp.oksocial.location;

import java.util.Optional;

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

  public static Optional<Location> latLong(double latitude, double longitude) {
    return Optional.of(new Location(latitude, longitude));
  }
}
