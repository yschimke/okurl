package com.baulsupp.oksocial.jjs;

import com.baulsupp.oksocial.Main;
import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.util.FileContent;
import java.io.IOException;
import java.util.Optional;
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
  public final OutputHandler outputHandler;

  private OkShell() throws Exception {
    main = new Main();
    main.initialise();
    client = main.getClient();
    requestBuilder = main.createRequestBuilder();
    outputHandler = main.outputHandler;

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

  public String query(String url) throws IOException {
    return execute(requestBuilder.url(url).build());
  }

  public String execute(Request request) throws IOException {
    Call call = client.newCall(request);

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
  }

  public Object credentials(String name) {
    if (main != null) {
      Optional<AuthInterceptor<?>> interceptor = main.interceptorByName(name);

      if (interceptor.isPresent()) {
        Optional<?> credentials =
            main.credentialsStore.readDefaultCredentials(interceptor.get().serviceDefinition());

        return credentials.orElse(null);
      }
    }

    return null;
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
