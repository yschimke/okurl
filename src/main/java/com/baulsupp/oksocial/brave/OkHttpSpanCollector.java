package com.baulsupp.oksocial.brave;

import com.github.kristofa.brave.AbstractSpanCollector;
import com.github.kristofa.brave.EmptySpanCollectorMetricsHandler;
import com.github.kristofa.brave.SpanCollectorMetricsHandler;
import com.twitter.zipkin.gen.SpanCodec;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * SpanCollector which submits spans to Zipkin, using its {@code POST /spans} endpoint.
 */
public final class OkHttpSpanCollector extends AbstractSpanCollector {
  private final String url;
  private OkHttpClient client;

  /**
   * Create a new instance with default configuration.
   *
   * @param baseUrl URL of the zipkin query server instance. Like: http://localhost:9411/
   * @param metrics Gets notified when spans are accepted or dropped. If you are not interested in
   * these events you can use {@linkplain EmptySpanCollectorMetricsHandler}
   */
  public static OkHttpSpanCollector create(String baseUrl, SpanCollectorMetricsHandler metrics) {
    return new OkHttpSpanCollector(baseUrl, metrics, new OkHttpClient());
  }

  private OkHttpSpanCollector(String baseUrl, SpanCollectorMetricsHandler metrics,
      OkHttpClient client) {
    super(SpanCodec.JSON, metrics, 0);
    this.url = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "api/v1/spans";
    this.client = client;
  }

  @Override
  protected void sendSpans(byte[] json) throws IOException {
    RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);
    Request request = new Request.Builder().url(url).method("POST", body).build();
    Response response = client.newCall(request).execute();

    if (response.isSuccessful()) {
      throw new IOException("failed to send to zipkin: " + response.message());
    }
  }
}
