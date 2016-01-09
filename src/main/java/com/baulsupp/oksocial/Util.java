package com.baulsupp.oksocial;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Util {
  private Util() {
  }

  public static String versionString(String propertiesFile) {
    try {
      Properties prop = new Properties();
      InputStream in = Main.class.getResourceAsStream(propertiesFile);
      prop.load(in);
      in.close();
      return prop.getProperty("version");
    } catch (IOException e) {
      throw new AssertionError("Could not load oksocial-version.properties.");
    }
  }

  public static boolean isOSX() {
    String osName = System.getProperty("os.name");
    return osName.contains("OS X");
  }
}
