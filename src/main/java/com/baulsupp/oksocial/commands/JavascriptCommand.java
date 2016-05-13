package com.baulsupp.oksocial.commands;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class JavascriptCommand implements ShellCommand {
  private Path scriptPath;

  public JavascriptCommand(Path scriptPath) {
    this.scriptPath = scriptPath;
  }

  @Override public String name() {
    return scriptPath.getFileName().toString();
  }

  @Override public List<Request> buildRequests(OkHttpClient.Builder clientBuilder,
      Request.Builder requestBuilder, List<String> urls) throws Exception {
    ScriptEngineManager engineManager =
        new ScriptEngineManager();
    ScriptEngine engine =
        engineManager.getEngineByName("nashorn");

    engine.eval("param = Java.type(\"com.baulsupp.oksocial.util.Params\").readParam;");

    engine.put("clientBuilder", clientBuilder);
    engine.put("requestBuilder", requestBuilder);
    engine.put("urls", urls);

    String lines = Files.lines(scriptPath, StandardCharsets.UTF_8).skip(1).collect(
        Collectors.joining("\n"));

    Object result = engine.eval(lines);

    if (result instanceof Request) {
      return Arrays.asList((Request) result);
    } else {
      throw new IllegalStateException("unable to use result " + result);
    }
  }
}
