package com.baulsupp.oksocial.brave;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.BraveExecutorService;
import com.github.kristofa.brave.InheritableServerClientAndLocalSpanState;
import com.github.kristofa.brave.LoggingSpanCollector;
import com.github.kristofa.brave.SpanCollector;
import com.github.kristofa.brave.okhttp.BraveTracingInterceptor;
import java.util.List;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

public abstract class BaseZipkinHandler extends ZipkinHandler {
  private Brave brave;

  public BaseZipkinHandler() {
  }

  protected abstract SpanCollector getSpanCollector();

  public static BaseZipkinHandler logging() {
    return new BaseZipkinHandler() {
      @Override protected SpanCollector getSpanCollector() {
        return new LoggingSpanCollector();
      }
    };
  }

  @Override public void configureClient(List<String> commandLineArgs,
      OkHttpClient.Builder clientBuilder) {
    com.twitter.zipkin.gen.Endpoint localEndpoint = com.twitter.zipkin.gen.Endpoint.builder()
        .serviceName("oksocial").build();

    brave = new Brave.Builder(
        new InheritableServerClientAndLocalSpanState(localEndpoint)).spanCollector(
        getSpanCollector())
        .build();

    BraveExecutorService tracePropagatingExecutor = new BraveExecutorService(
        clientBuilder.build().dispatcher().executorService(),
        brave.serverSpanThreadBinder()
    );

    BraveTracingInterceptor tracingInterceptor =
        BraveTracingInterceptor.builder(brave).parser(new OkSocialParser()).build();

    // TODO how to create a span around the whole command line
    //brave.clientTracer().submitBinaryAnnotation("cmdline", Joiner.on(" ").join(commandLineArgs));

    clientBuilder.addInterceptor(tracingInterceptor);
    clientBuilder.addNetworkInterceptor(tracingInterceptor);
    clientBuilder.dispatcher(new Dispatcher(tracePropagatingExecutor));
  }
}
