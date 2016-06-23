package com.baulsupp.oksocial.jjs;

import com.baulsupp.oksocial.commands.ShellCommand;
import com.baulsupp.oksocial.util.UsageException;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class JavascriptApiCommand implements ShellCommand {
  public JavascriptApiCommand() {
  }

  @Override public String name() {
    return "okapi";
  }

  @Override public List<Request> buildRequests(OkHttpClient client,
      Request.Builder requestBuilder, List<String> arguments) throws Exception {
    boolean multiple = false;

    if (arguments.get(0).equals("-m")) {
      multiple = true;
      arguments.remove(0);
    }

    Path script = FileSystems.getDefault().getPath(arguments.remove(0));

    ScriptEngineManager engineManager =
        new ScriptEngineManager();
    ScriptEngine engine =
        engineManager.getEngineByName("nashorn");

    eval(engine, "param = Java.type(\"com.baulsupp.oksocial.jjs.OkShell\").readParam;");

    engine.put("client", client);
    engine.put("clientBuilder", client.newBuilder());
    engine.put("requestBuilder", requestBuilder);

    String lines = Files.lines(script, StandardCharsets.UTF_8).skip(1).collect(
        Collectors.joining("\n"));

    if (multiple) {
      // TODO how to do this without engine.eval
      engine.put("a", arguments);
      Object argumentsJs = engine.eval("Java.from(a)");

      engine.put("arguments", argumentsJs);

      Object result = eval(engine, lines);

      return toRequestList(requestBuilder, result);
    } else {
      return arguments.stream().map(item -> {
        engine.put("item", item);
        Object result = eval(engine, lines);

        return toRequest(requestBuilder, result);
      }).collect(Collectors.toList());
    }
  }

  public Object eval(ScriptEngine engine, String script) {
    try {
      return engine.eval(script);
    } catch (ScriptException e) {
      Throwable cause = e.getCause();
      while (cause != null) {
        if (cause instanceof UsageException) {
          throw (UsageException) cause;
        }
        cause = cause.getCause();
      }

      throw Throwables.propagate(e);
    }
  }

  private List<Request> toRequestList(Request.Builder requestBuilder, Object result) {
    if (result instanceof ScriptObjectMirror) {
      ScriptObjectMirror m = (ScriptObjectMirror) result;

      List<Request> list = Lists.newArrayList();

      for (Object o : m.values()) {
        list.add(toRequest(requestBuilder, o));
      }

      return list;
    } else {
      return Arrays.asList(toRequest(requestBuilder, result));
    }
  }

  private Request toRequest(Request.Builder requestBuilder, Object o) {
    if (o instanceof Request) {
      return (Request) o;
    } else if (o instanceof String) {
      return requestBuilder.url((String) o).build();
    } else if (o == null) {
      throw new NullPointerException();
    } else {
      throw new IllegalStateException("unable to use result " + o + " " + o.getClass());
    }
  }
}
