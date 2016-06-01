package com.baulsupp.oksocial.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.zeroturnaround.exec.ProcessExecutor;

public class CommandUtil {
  private static LoadingCache<String, Boolean> installed =
      CacheBuilder.newBuilder().build(new CacheLoader<String, Boolean>() {
        @Override public Boolean load(String command) throws Exception {
          return isInstalledInternal(command);
        }
      });

  private static boolean isInstalledInternal(String command)
      throws InterruptedException, TimeoutException, IOException {
    return new ProcessExecutor().command("command", "-v", command).execute().getExitValue() == 0;
  }

  public static boolean isInstalled(String command) {
    try {
      return installed.get(command);
    } catch (ExecutionException e) {
      e.getCause().printStackTrace();
      return false;
    }
  }

  public static boolean isTerminal() {
    return System.console() != null;
  }
}
