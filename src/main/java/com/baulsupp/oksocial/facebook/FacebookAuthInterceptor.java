package com.baulsupp.oksocial.facebook;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.OSXCredentialsStore;
import java.io.IOException;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FacebookAuthInterceptor implements AuthInterceptor {
  private final CredentialsStore<FacebookCredentials> credentialsStore;
  private FacebookCredentials credentials = null;

  public FacebookAuthInterceptor() {
    credentialsStore = CredentialsStore.create(new FacebookOSXCredentials());
  }

  @Override public String mapUrl(String alias, String url) {
    switch (alias) {
      case "fbgraph":
        return "https://graph.facebook.com" + url;
      default:
        return null;
    }
  }

  private FacebookCredentials getCredentials() {
    if (credentials == null) {
      credentials = credentialsStore.readDefaultCredentials();
    }

    return credentials;
  }

  @Override public Response intercept(Interceptor.Chain chain) throws IOException {
    Request request = chain.request();

    String token = getCredentials().accessToken;
    //      token = "CAAH0ZBiEkcg8BAPp8tVQsegHnJUgw6gifx63JfF4LRwZA5xZCl58HAzpYiZADDKIkU0xA4sIKjGO4pYwLOYm3QarQ2r0nvfQb4qp8FHSoFl8zzLTv1xuUDxup2xe3SSgN5JtAofMjL27P1ZC1s2sg1izAczUcvaINVlIa5YHje58sByuViQAK1jle573k8uKNw3OVMUEtpfe4aZAB2ZA5s5iZBkU0iJkn7UZD";

    HttpUrl newUrl = request.url().newBuilder().addQueryParameter("access_token", token).build();

    request =
        request.newBuilder().url(newUrl).build();

    return chain.proceed(request);
  }

  public boolean supportsUrl(HttpUrl url) {
    String host = url.host();

    return FacebookUtil.API_HOSTS.contains(host);
  }

  @Override public void authorize(OkHttpClient client) {
    System.err.println("Authorising Facebook API");
    FacebookCredentials newCredentials = LoginAuthFlow.login(client);
    CredentialsStore<FacebookCredentials> facebookCredentialsStore =
        new OSXCredentialsStore<>(new FacebookOSXCredentials());
    facebookCredentialsStore.storeCredentials(newCredentials);
  }
}
