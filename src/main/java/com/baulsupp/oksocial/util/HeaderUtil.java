package com.baulsupp.oksocial.util;

import com.baulsupp.oksocial.output.util.UsageException;
import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import static com.baulsupp.oksocial.util.FileUtil.expectedFile;

// TODO handle duplicate header keys
public class HeaderUtil {
  public static Map<String, String> headerMap(@Nullable List<String> headers) {
    if (headers == null) {
      return Collections.emptyMap();
    }

    Map<String, String> headerMap = new LinkedHashMap<>();

    if (headers != null) {
      for (String header : headers) {
        if (header.startsWith("@")) {
          headerMap.putAll(headerFileMap(header));
        } else {
          String[] parts = header.split(":", 2);
          // TODO: consider better strategy than simple trim
          String name = parts[0].trim();
          String value = stringValue(parts[1].trim());
          headerMap.put(name, value);
        }
      }
    }
    return headerMap;
  }

  private static Map<? extends String, ? extends String> headerFileMap(String input) {
    try {
      return headerMap(Files.readLines(inputFile(input), Charsets.UTF_8));
    } catch (IOException ioe) {
      throw new UsageException("failed to read header file", ioe);
    }
  }

  public static String stringValue(String source) {
    if (source.startsWith("@")) {
      try {
        return Files.toString(inputFile(source), StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new UsageException(e.toString());
      }
    } else {
      return source;
    }
  }

  public static File inputFile(String path) {
    return expectedFile(path.substring(1));
  }
}
