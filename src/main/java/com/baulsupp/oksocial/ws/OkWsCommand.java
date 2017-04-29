package com.baulsupp.oksocial.ws;

import com.baulsupp.oksocial.Main;
import com.baulsupp.oksocial.commands.MainAware;
import com.baulsupp.oksocial.commands.ShellCommand;
import com.google.common.collect.Lists;
import com.baulsupp.oksocial.output.util.UsageException;
import java.util.List;
import java.util.Scanner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class OkWsCommand implements ShellCommand, MainAware {
  private Main main;

  @Override public void setMain(Main main) {
    this.main = main;
  }

  @Override public String name() {
    return "okws";
  }

  @Override public boolean handlesRequests() {
    return true;
  }

  @Override public List<Request> buildRequests(OkHttpClient client, Request.Builder requestBuilder,
      List<String> arguments) throws Exception {
    if (arguments.size() != 1) {
      throw new UsageException("usage: okws wss://host");
    }

    Request request = main.createRequestBuilder().url(arguments.get(0)).build();

    WebSocketPrinter printer = new WebSocketPrinter(main.outputHandler);
    WebSocket websocket = client.newWebSocket(request, printer);

    Scanner sc = new Scanner(System.in);
    while (sc.hasNextLine()) {
      String line = sc.nextLine();
      websocket.send(line);
    }

    printer.waitForExit();

    return Lists.newArrayList();
  }
}
