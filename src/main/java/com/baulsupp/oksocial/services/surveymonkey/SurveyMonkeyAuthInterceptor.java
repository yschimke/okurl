package com.baulsupp.oksocial.services.surveymonkey;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.secrets.Secrets;
import com.baulsupp.oksocial.services.UrlList;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

  @Override public List<String> matchingUrls(String prefix, OkHttpClient client,
      CredentialsStore credentialsStore)
      throws IOException {
    UrlList urlList = UrlList.fromResource("surveymonkey");

    Optional<SurveyMonkeyToken> credentials =
        credentialsStore.readDefaultCredentials(serviceDefinition());

    if (credentials.isPresent()) {
      List<String> surveys = getSurveyIds(client);

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

    return urlList.matchingUrls(prefix);
  }

  private List<String> getSurveyIds(OkHttpClient client) throws IOException {
    try {
      Map<String, Object> map = AuthUtil.makeJsonMapRequest(client,
          new Request.Builder().url("https://api.surveymonkey.net/v3/surveys").build());

      List<Map<String, Object>> surveys = (List<Map<String, Object>>) map.get("data");

      return surveys.stream().map(m -> (String) m.get("id")).collect(toList());
    } catch (IOException ioe) {
      return Lists.newArrayList();
    }
  }

  @Override public Collection<? extends String> hosts() {
    return SurveyMonkeyUtil.API_HOSTS;
  }
}
