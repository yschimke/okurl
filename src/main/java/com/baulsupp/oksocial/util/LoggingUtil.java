package com.baulsupp.oksocial.util;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import okhttp3.internal.http2.Http2;

public class LoggingUtil {
  private static List<Logger> activeLoggers = Lists.newArrayList();

  public static void configureLogging(boolean debug, boolean showHttp2Frames) {
    if (debug || showHttp2Frames) {
      LogManager.getLogManager().reset();
      ConsoleHandler handler = new ConsoleHandler();

      if (debug) {
        handler.setLevel(Level.ALL);
        handler.setFormatter(new OneLineLogFormat());
        Logger activeLogger = getLogger("");
        activeLogger.addHandler(handler);
        activeLogger.setLevel(Level.ALL);

        Logger x = getLogger("org.zeroturnaround.exec.stream");
        x.setLevel(Level.INFO);
      } else if (showHttp2Frames) {
        Logger activeLogger = getLogger(Http2.class.getName());
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

  public static Logger getLogger(String name) {
    Logger logger = Logger.getLogger(name);
    activeLoggers.add(logger);
    return logger;
  }
}
