package com.baulsupp.oksocial.jjs;

import com.baulsupp.oksocial.Main;
import com.baulsupp.oksocial.util.FileContent;
import com.google.common.base.Throwables;
import java.io.IOException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkShell {
  public final OkHttpClient client;
  public final Request.Builder requestBuilder;
  private final ScriptEngine engine;
  private final Main main;

  private OkShell() throws Exception {
    main = new Main();
    //main.debug = true;
    client = main.createClientBuilder().build();
    requestBuilder = main.createRequestBuilder();

    ScriptEngineManager m = new ScriptEngineManager();
    engine = m.getEngineByName("nashorn");

    listenForMainExit();
  }

  public void listenForMainExit() {
    Thread main = Thread.currentThread();

    Thread t = new Thread(() -> {
      try {
        main.join();
      } catch (InterruptedException e) {
      }

      close();
    }, "exit listener");
    t.setDaemon(true);
    t.start();
  }

  public String query(String url) {
    try {
      Call call = client.newCall(requestBuilder.url(url).build());

      Response response = call.execute();

      try {
        String responseString = response.body().string();

        if (!response.isSuccessful()) {
          throw new RuntimeException(responseString);
        }

        return responseString;
      } finally {
        response.body().close();
      }
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  private void close() {
    client.connectionPool().evictAll();
  }

  public static OkShell instance() throws Exception {
    return new OkShell();
  }

  public String toString() {
    return "OkShell";
  }

  public static String readParam(String param) throws IOException {
    return FileContent.readParamString(param);
  }
}
