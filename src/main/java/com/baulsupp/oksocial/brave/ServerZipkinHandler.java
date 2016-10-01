package com.baulsupp.oksocial.brave;

import com.baulsupp.oksocial.output.OutputHandler;
import com.github.kristofa.brave.EmptySpanCollectorMetricsHandler;
import com.github.kristofa.brave.IdConversion;
import com.github.kristofa.brave.SpanCollector;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;

public class ServerZipkinHandler extends BaseZipkinHandler {
  private final OkHttpSpanCollector httpCollector;
  private Set<String> spanIds = Sets.newLinkedHashSet();
  private InetSocketAddress address;
  private OutputHandler outputHandler;

  public ServerZipkinHandler(InetSocketAddress address, OutputHandler outputHandler) {
    this.address = address;
    this.outputHandler = outputHandler;

    httpCollector =
        OkHttpSpanCollector.create(getServer(address), new EmptySpanCollectorMetricsHandler());
  }

  protected SpanCollector getSpanCollector() {
    return new CollectingSpanCollector(httpCollector, span -> {
      spanIds.add(IdConversion.convertToString(span.getTrace_id()));
    });
  }

  public static ServerZipkinHandler localhost(OutputHandler outputHandler) {
    return instance(InetSocketAddress.createUnresolved("localhost", 9411), outputHandler);
  }

  public static ServerZipkinHandler instance(InetSocketAddress address,
      OutputHandler outputHandler) {
    return new ServerZipkinHandler(address, outputHandler);
  }

  @Override public void close() throws IOException {
    httpCollector.flush();
    httpCollector.close();

    for (String spanId : spanIds) {
      outputHandler.openLink(
          ServerZipkinHandler.getServer(address) + "traces/" + spanId);
    }
  }

  public static String getServer(InetSocketAddress address) {
    return "http://" + address.getHostName() + ":" + address.getPort() + "/";
  }
}
