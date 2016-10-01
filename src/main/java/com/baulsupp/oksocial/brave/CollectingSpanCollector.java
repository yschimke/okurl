package com.baulsupp.oksocial.brave;

import com.github.kristofa.brave.SpanCollector;
import com.twitter.zipkin.gen.Span;
import java.util.function.Consumer;

public class CollectingSpanCollector implements SpanCollector {
  private SpanCollector delegate;
  private Consumer<Span> spanCallback;

  public CollectingSpanCollector(SpanCollector delegate, Consumer<Span> spanCallback) {
    this.delegate = delegate;
    this.spanCallback = spanCallback;
  }

  @Override public void collect(Span span) {
    spanCallback.accept(span);
    delegate.collect(span);
  }

  @Override public void addDefaultAnnotation(String key, String value) {
    delegate.addDefaultAnnotation(key, value);
  }
}
