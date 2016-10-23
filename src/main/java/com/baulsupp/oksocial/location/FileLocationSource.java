package com.baulsupp.oksocial.location;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class FileLocationSource implements LocationSource {
  public static File FILE = new File(System.getenv("HOME"), ".oksocial-location.json");
  private File file;

  public FileLocationSource(File file) {
    this.file = file;
  }

  @Override public Optional<Location> read() throws IOException {
    if (file.isFile()) {
      ObjectMapper mapper = new ObjectMapper();

      Map<String, Double> values = mapper.readValue(file, new TypeReference<Map<String, Double>>() {
      });

      return Optional.of(new Location(values.get("latitude"), values.get("longitude")));
    }

    return Optional.empty();
  }

  public void save(Location location) throws IOException {
    ObjectMapper mapper = new ObjectMapper();

    Map<String, Double> map = Maps.newLinkedHashMap();
    map.put("latitude", location.latitude);
    map.put("longitude", location.longitude);

    mapper.writeValue(file, map);
  }
}
