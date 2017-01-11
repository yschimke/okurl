package com.baulsupp.oksocial.authenticator;

import com.baulsupp.oksocial.apidocs.ApiDocPresenter;
import com.baulsupp.oksocial.completion.ApiCompleter;
import com.baulsupp.oksocial.completion.BaseUrlCompleter;
import com.baulsupp.oksocial.completion.CompletionVariableCache;
import com.baulsupp.oksocial.completion.HostUrlCompleter;
import com.baulsupp.oksocial.completion.UrlList;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.output.OutputHandler;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.util.Optional.empty;

public interface AuthInterceptor<T> {
  Logger logger = Logger.getLogger(AuthInterceptor.class.getName());

  default String name() {
    return serviceDefinition().shortName();
  }

  default boolean supportsUrl(HttpUrl url) {
    try {
      return hosts().contains(url.host());
    } catch (IOException e) {
      logger.log(Level.WARNING, "failed getting hosts", e);
      return false;
    }
  }

  Response intercept(Interceptor.Chain chain, T credentials) throws IOException;

  T authorize(OkHttpClient client, OutputHandler outputHandler, List<String> authArguments)
      throws IOException;

  ServiceDefinition<T> serviceDefinition();

  Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, T credentials) throws IOException;

  default boolean canRenew(Response result, T credentials) {
    return false;
  }

  default Optional<T> renew(OkHttpClient client, T credentials) throws IOException {
    return empty();
  }

  Collection<String> hosts() throws IOException;

  default ApiCompleter apiCompleter(String prefix, OkHttpClient client,
      CredentialsStore credentialsStore, CompletionVariableCache completionVariableCache)
      throws IOException {
    Optional<UrlList> urlList = UrlList.fromResource(name());

    if (urlList.isPresent()) {
      return new BaseUrlCompleter(urlList.get(), hosts());
    } else {
      return new HostUrlCompleter(hosts());
    }
  }

  default Optional<T> defaultCredentials() {
    return empty();
  }

  default ApiDocPresenter apiDocPresenter(String url) {
    return new ApiDocPresenter() {
      @Override public void explainApi(String url, OutputHandler outputHandler, OkHttpClient client)
          throws IOException {
        ServiceDefinition<T> sd = serviceDefinition();

        outputHandler.info("service: " + sd.shortName());
        outputHandler.info("name: " + sd.serviceName());
        sd.apiDocs().ifPresent(d -> outputHandler.info("docs: " + d));
      }
    };
  }
}
