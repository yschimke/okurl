package com.baulsupp.oksocial.services.surveymonkey;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.completion.CompletionCache;
import com.baulsupp.oksocial.completion.CompletionQuery;
import com.baulsupp.oksocial.completion.UrlList;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.baulsupp.oksocial.authenticator.JsonCredentialsValidator.fieldExtractor;
import static java.util.stream.Collectors.toList;

/**
 * https://developer.surveymonkey.com/docs/authentication
 */
public class SurveyMonkeyAuthInterceptor implements AuthInterceptor<SurveyMonkeyToken> {
  @Override public ServiceDefinition<SurveyMonkeyToken> serviceDefinition() {
    return new SurveyMonkeyServiceDefinition();
  }

  @Override public Response intercept(Interceptor.Chain chain, SurveyMonkeyToken credentials)
      throws IOException {
    Request request = chain.request();

    String token = credentials.accessToken;

    HttpUrl newUrl =
        request.url().newBuilder().addQueryParameter("api_key", credentials.apiKey).build();
    request =
        request.newBuilder().addHeader("Authorization", "bearer " + token).url(newUrl).build();

    return chain.proceed(request);
  }

  @Override public SurveyMonkeyToken authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments) throws IOException {
    System.err.println("Authorising SurveyMonkey API");

    String apiKey =
        Secrets.prompt("SurveyMonkey API Key", "surveymonkey.apiKey", "", false);
    String accessToken =
        Secrets.prompt("SurveyMonkey Access Token", "surveymonkey.accessToken", "", true);
    return new SurveyMonkeyToken(apiKey, accessToken);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, SurveyMonkeyToken credentials) throws IOException {
    return new JsonCredentialsValidator(
        SurveyMonkeyUtil.apiRequest("/v3/users/me", requestBuilder),
        fieldExtractor("username")).validate(client);
  }

  @Override public Future<List<String>> matchingUrls(String prefix, OkHttpClient client,
      CredentialsStore credentialsStore, CompletionCache completionCache, boolean expensive)
      throws IOException {
    UrlList urlList = UrlList.fromResource("surveymonkey").get();

    Optional<SurveyMonkeyToken> credentials =
        credentialsStore.readDefaultCredentials(serviceDefinition());

    if (credentials.isPresent()) {
      Optional<List<String>> surveysOpt =
          completionCache.get(serviceDefinition().shortName(), "surveys", expensive);

      List<String> surveys;
      if (!surveysOpt.isPresent()) {
        surveys = CompletionQuery.getIds(client, "https://api.surveymonkey.net/v3/surveys", "data",
            "id");
        completionCache.store(serviceDefinition().shortName(), "surveys", surveys);
      } else {
        surveys = surveysOpt.get();
      }

      if (!surveys.isEmpty()) {
        List<String> urls = urlList.getUrls();

        List<String> newUrls = urls.stream()
            .flatMap(
                u -> u.contains("{survey}") ? surveys.stream().map(s -> u.replace("{survey}", s))
                    : Stream.of(u))
            .collect(toList());

        urlList = new UrlList(newUrls);
      }
    }

    return CompletableFuture.completedFuture(urlList.matchingUrls(prefix));
  }

  @Override public Collection<? extends String> hosts() {
    return SurveyMonkeyUtil.API_HOSTS;
  }
}
