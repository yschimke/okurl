package okhttp.twitter;

import com.baulsupp.oksocial.InsecureHostnameVerifier;
import com.baulsupp.oksocial.InsecureTrustManager;
import com.baulsupp.oksocial.OpenSCUtil;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OpenSCUtilTesting {

  public static void main(String[] args)
      throws Exception {
    OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

    // Test only
    clientBuilder.hostnameVerifier(new InsecureHostnameVerifier());
    TrustManager[] trustManager = new TrustManager[] {new InsecureTrustManager()};

    char[] password = System.console().readPassword("smartcard password: ");
    KeyManager[] keyManagers = OpenSCUtil.getKeyManagers(password);

    SSLContext context = SSLContext.getInstance("TLS");
    context.init(keyManagers, trustManager, null);
    SSLSocketFactory socketFactory = context.getSocketFactory();

    OkHttpClient client = clientBuilder.sslSocketFactory(socketFactory).build();

    Request request = new Request.Builder().url("https://localhost:44330/").build();

    Response response = client.newCall(request).execute();

    try {
      System.out.println(response.body().string());
    } finally {
      response.body().close();
    }
  }
}
