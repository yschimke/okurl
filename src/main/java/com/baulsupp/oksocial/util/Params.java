package com.baulsupp.oksocial.util;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

public class Params {
  public static String readParam(String param) throws IOException {
    if (param.startsWith("@")) {
      return FileUtils.readFileToString(new File(param.substring(1)));
    } else {
      return param;
    }
  }
}
