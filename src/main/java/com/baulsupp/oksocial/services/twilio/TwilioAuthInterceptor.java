package com.baulsupp.oksocial.services.twilio;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.BasicCredentials;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.List;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TwilioAuthInterceptor implements AuthInterceptor<BasicCredentials> {
  @Override public ServiceDefinition<BasicCredentials> serviceDefinition() {
    return new TwilioServiceDefinition();
  }

  @Override
  public Response intercept(Interceptor.Chain chain, BasicCredentials credentials)
      throws IOException {
    Request request = chain.request();

    request =
        request.newBuilder()
            .addHeader("Authorization", Credentials.basic(credentials.user, credentials.password))
            .build();

    return chain.proceed(request);
  }

  public boolean supportsUrl(HttpUrl url) {
    String host = url.host();

    return TwilioUtil.API_HOSTS.contains(host);
  }

  @Override public BasicCredentials authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments) {
    String user =
        Secrets.prompt("Twilio Account SID", "twilio.accountSid", "", false);
    String password =
        Secrets.prompt("Twilio Auth Token", "twilio.authToken", "", true);

    return new BasicCredentials(user, password);
  }
}
