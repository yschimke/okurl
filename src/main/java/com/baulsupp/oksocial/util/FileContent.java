package com.baulsupp.oksocial.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class FileContent {

  public static byte[] readParamBytes(String param) throws IOException {
    if (param.equals("@-")) {
      return IOUtils.toByteArray(System.in);
    }
    if (param.startsWith("@")) {
      return FileUtils.readFileToByteArray(new File(param.substring(1)));
    } else {
      return param.getBytes(StandardCharsets.UTF_8);
    }
  }

  public static String readParamString(String param) throws IOException {
    if (param.equals("@-")) {
      return IOUtils.toString(System.in);
    }
    if (param.startsWith("@")) {
      return FileUtils.readFileToString(new File(param.substring(1)));
    } else {
      return param;
    }
  }
}
