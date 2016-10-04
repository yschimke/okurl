package com.baulsupp.oksocial.brave;

import com.github.kristofa.brave.SpanCollector;
import com.twitter.zipkin.gen.Span;
import java.util.function.Consumer;
import zipkin.reporter.Reporter;

public class CollectingSpanCollector implements SpanCollector {
  private SpanCollector delegate;
  private Reporter<Span> spanCallback;

  public CollectingSpanCollector(SpanCollector delegate, Reporter<Span> spanCallback) {
    this.delegate = delegate;
    this.spanCallback = spanCallback;
  }

  @Override public void collect(Span span) {
    spanCallback.report(span);
    delegate.collect(span);
  }

  @Override public void addDefaultAnnotation(String s, String s1) {
  }
}
