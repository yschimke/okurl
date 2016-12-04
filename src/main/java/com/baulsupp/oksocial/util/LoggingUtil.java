package com.baulsupp.oksocial.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import okhttp3.internal.http2.Http2;

public class LoggingUtil {
  private static Logger activeLogger;

  public static void configureLogging(boolean debug, boolean showHttp2Frames) {
    if (debug || showHttp2Frames) {
      LogManager.getLogManager().reset();
      ConsoleHandler handler = new ConsoleHandler();

      if (debug) {
        handler.setLevel(Level.ALL);
        handler.setFormatter(new OneLineLogFormat());
        activeLogger = Logger.getLogger("");
        activeLogger.addHandler(handler);
        activeLogger.setLevel(Level.ALL);
      } else {
        activeLogger = Logger.getLogger(Http2.class.getName());
        activeLogger.setLevel(Level.FINE);
        handler.setLevel(Level.FINE);
        handler.setFormatter(new SimpleFormatter() {
          @Override public String format(LogRecord record) {
            return String.format("%s%n", record.getMessage());
          }
        });
        activeLogger.addHandler(handler);
      }
    }
  }
}
