package com.baulsupp.oksocial.tracing;

import com.baulsupp.oksocial.output.util.UsageException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import zipkin.Span;
import zipkin.reporter.Reporter;

import static com.baulsupp.oksocial.tracing.UriHandler.loadServices;

/**
 * Registry for looking up transports by URI.
 *
 * <p>Uses the Jar Services mechanism with services defined by {@link UriHandler}.
 */
public class UriTransportRegistry {
  private List<UriHandler> handlers;

  public UriTransportRegistry(ServiceLoader<UriHandler> services) {
    handlers = new ArrayList<>();
    services.forEach(handlers::add);
  }

  public static UriTransportRegistry fromServices() {
    ServiceLoader<UriHandler> services = loadServices();

    return new UriTransportRegistry(services);
  }

  public static Reporter<Span> forUri(String uri) {
    return UriTransportRegistry.fromServices().findClient(uri);
  }

  private Reporter<Span> findClient(String uriString) {
    URI uri = URI.create(uriString);

    for (UriHandler h : handlers) {
      Optional<Reporter<Span>> r = h.buildSender(uri);
      if (r.isPresent()) {
        return r.get();
      }
    }

    throw new UsageException("unknown zipkin sender: " + uriString);
  }
}