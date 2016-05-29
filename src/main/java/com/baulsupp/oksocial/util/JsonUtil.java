package com.baulsupp.oksocial.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;

public class JsonUtil {
  public static Map<String, Object> map(String content) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(content, new TypeReference<Map<String, Object>>() {
    });
  }

  public static String toJson(Map<String, String> map) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(map);
  }
}
