package com.baulsupp.oksocial.services.twitter;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.secrets.Secrets;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.twitter.joauth.Normalizer;
import com.twitter.joauth.OAuthParams;
import com.twitter.joauth.Signer;
import com.twitter.joauth.UrlCodec;
import com.twitter.joauth.keyvalue.KeyValueHandler;
import com.twitter.joauth.keyvalue.KeyValueParser;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class TwitterAuthInterceptor implements AuthInterceptor<TwitterCredentials> {
  private static final Logger log = Logger.getLogger(TwitterAuthInterceptor.class.getName());

  private final SecureRandom secureRandom = new SecureRandom();

  @Override public ServiceDefinition<TwitterCredentials> serviceDefinition() {
    return new TwitterServiceDefinition();
  }

  public boolean supportsUrl(HttpUrl url) {
    return TwitterUtil.TWITTER_API_HOSTS.contains(url.host());
  }

  @Override
  public Response intercept(Interceptor.Chain chain, Optional<TwitterCredentials> credentials)
      throws IOException {
    Request request = chain.request();

    if (credentials.isPresent()) {
      String authHeader = generateAuthorization(request, credentials.get());
      request = request.newBuilder().addHeader("Authorization", authHeader).build();
    }

    return chain.proceed(request);
  }

  private String quoted(String str) {
    return "\"" + str + "\"";
  }

  public long generateTimestamp() {
    long timestamp = System.currentTimeMillis();
    return timestamp / 1000;
  }

  public String generateNonce() {
    return Long.toString(Math.abs(secureRandom.nextLong())) + System.currentTimeMillis();
  }

  public String generateAuthorization(Request request, TwitterCredentials credentials) {
    try {
      long timestampSecs = generateTimestamp();
      String nonce = generateNonce();

      Normalizer normalizer = Normalizer.getStandardNormalizer();
      Signer signer = Signer.getStandardSigner();

      OAuthParams.OAuth1Params oAuth1Params = new OAuthParams.OAuth1Params(
          credentials.token, credentials.consumerKey, nonce, timestampSecs,
          Long.toString(timestampSecs), "", OAuthParams.HMAC_SHA1, OAuthParams.ONE_DOT_OH
      );

      List<com.twitter.joauth.Request.Pair> javaParams = new ArrayList<>();

      Set<String> queryParamNames = request.url().queryParameterNames();
      for (String queryParam : queryParamNames) {
        List<String> values = request.url().queryParameterValues(queryParam);

        for (String value : values) {
          javaParams.add(new com.twitter.joauth.Request.Pair(UrlCodec.encode(queryParam),
              UrlCodec.encode(value)));
        }
      }

      if (request.method().equals("POST")) {
        RequestBody body = request.body();

        if (body instanceof FormBody) {
          FormBody formBody = (FormBody) body;

          for (int i = 0; i < formBody.size(); i++) {
            javaParams.add(new com.twitter.joauth.Request.Pair(formBody.encodedName(i),
                formBody.encodedValue(i)));
          }
        } else if (isFormContentType(request)) {
          Buffer buffer = new Buffer();
          body.writeTo(buffer);
          String encodedBody = buffer.readString(Charset.forName("UTF-8"));

          KeyValueHandler.DuplicateKeyValueHandler handler =
              new KeyValueHandler.DuplicateKeyValueHandler();

          KeyValueParser.StandardKeyValueParser bodyParser =
              new KeyValueParser.StandardKeyValueParser("&", "=");
          bodyParser.parse(encodedBody, Collections.singletonList(handler));

          javaParams.addAll(handler.toList());
        }
      }

      String normalized = normalizer.normalize(
          request.isHttps() ? "https" : "http", request.url().host(), request.url().port(),
          request.method(), request.url().encodedPath(), javaParams, oAuth1Params
      );

      log.log(Level.FINE, "normalised " + normalized);
      log.log(Level.FINE, "secret " + credentials.secret);
      log.log(Level.FINE, "consumerSecret " + credentials.consumerSecret);

      String signature =
          signer.getString(normalized, credentials.secret, credentials.consumerSecret);

      Map<String, String> oauthHeaders = new LinkedHashMap<>();
      oauthHeaders.put(OAuthParams.OAUTH_CONSUMER_KEY, quoted(credentials.consumerKey));
      oauthHeaders.put(OAuthParams.OAUTH_NONCE, quoted(nonce));
      oauthHeaders.put(OAuthParams.OAUTH_SIGNATURE, quoted(signature));
      oauthHeaders.put(OAuthParams.OAUTH_SIGNATURE_METHOD, quoted(OAuthParams.HMAC_SHA1));
      oauthHeaders.put(OAuthParams.OAUTH_TIMESTAMP, quoted(Long.toString(timestampSecs)));
      if (credentials.token != null) {
        oauthHeaders.put(OAuthParams.OAUTH_TOKEN, quoted(credentials.token));
      }
      oauthHeaders.put(OAuthParams.OAUTH_VERSION, quoted(OAuthParams.ONE_DOT_OH));

      return "OAuth " + Joiner.on(", ").withKeyValueSeparator("=").join(oauthHeaders);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  private boolean isFormContentType(Request request) {
    return request.body().contentType().toString().startsWith("application/x-www-form-urlencoded");
  }

  @Override
  public TwitterCredentials authorize(OkHttpClient client, List<String> authArguments)
      throws IOException {
    System.err.println("Authorising Twitter API");
    return PinAuthorisationFlow.authorise(client, readClientCredentials());
  }

  public static TwitterCredentials readClientCredentials() {
    String consumerKey = Secrets.prompt("Consumer Key", "twitter.consumerKey", "", false);
    String consumerSecret = Secrets.prompt("Consumer Secret", "twitter.consumerSecret", "", true);

    return new TwitterCredentials(null, consumerKey, consumerSecret, null, "");
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, TwitterCredentials credentials) throws IOException {
    return new JsonCredentialsValidator(
        TwitterUtil.apiRequest("/1.1/account/verify_credentials.json", requestBuilder),
        map -> (String) map.get("name")).validate(client);
  }
}
