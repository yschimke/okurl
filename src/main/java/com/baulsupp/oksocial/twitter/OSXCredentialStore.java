package com.baulsupp.oksocial.twitter;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import okio.BufferedSource;
import okio.Okio;

public class OSXCredentialStore implements CredentialsStore {
  @Override public TwitterCredentials readDefaultCredentials() throws IOException {
    // security find-generic-password -a "api.twitter.com" -D "oauth credentials" -w

    Process process =
        new ProcessBuilder("/usr/bin/security", "find-generic-password", "-a", "api.twitter.com",
            "-D", "oauth credentials", "-w").redirectError(ProcessBuilder.Redirect.INHERIT).start();

    try {
      process.getOutputStream().close();
      // may timeout if password is somehow longer than the pipe can hold
      if (!process.waitFor(10, TimeUnit.SECONDS)) {
        process.destroyForcibly();
        throw new IOException("timeout calling /usr/bin/security");
      }

      if (process.exitValue() == 44) {
        return null;
      }

      if (process.exitValue() != 0) {
        throw new IOException("/usr/bin/security exited with return code " + process.exitValue());
      }

      BufferedSource stdout = Okio.buffer(Okio.source(process.getInputStream()));

      return parseCredentialsString(stdout.readUtf8LineStrict());
    } catch (InterruptedException e) {
      throw new IOException(e);
    } finally {
      if (process.isAlive()) {
        process.destroyForcibly();
      }
    }
  }

  @Override public void storeCredentials(TwitterCredentials credentials) throws IOException {
    // security add-generic-password -a "api.twitter.com" -s "Twitter API" -D "oauth credentials" -U -w abc

    String credentialsString = formatCredentialsString(credentials);

    Process process =
        new ProcessBuilder("/usr/bin/security", "add-generic-password", "-a", "api.twitter.com",
            "-D", "oauth credentials", "-s", "Twitter API", "-U", "-w", credentialsString).redirectError(
            ProcessBuilder.Redirect.INHERIT).redirectInput(ProcessBuilder.Redirect.INHERIT).start();

    try {
      process.getOutputStream().close();
      // may timeout if password is somehow longer than the pipe can hold
      if (!process.waitFor(10, TimeUnit.SECONDS)) {
        process.destroyForcibly();
        throw new IOException("timeout calling /usr/bin/security");
      }
    } catch (InterruptedException e) {
      throw new IOException(e);
    } finally {
      if (process.isAlive()) {
        process.destroyForcibly();
      }
    }
  }

  private TwitterCredentials parseCredentialsString(String s) {
    List<String> list = Splitter.on(",").splitToList(s);

    if (list.size() != 5) {
      throw new IllegalStateException("can't split '" + s + "'");
    }

    return new TwitterCredentials(list.get(0), list.get(1), list.get(2), list.get(3), list.get(4));
  }

  private String formatCredentialsString(TwitterCredentials credentials) {
    return Joiner.on(",")
        .join(credentials.username, credentials.consumerKey, credentials.consumerSecret,
            credentials.token, credentials.secret);
  }

  public static void main(String[] args) throws IOException {
    TwurlCredentialsStore orig = new TwurlCredentialsStore(new File(System.getenv("HOME"), ".twurlrc"));

    TwitterCredentials creds = orig.readDefaultCredentials();

    System.out.println(creds);

    OSXCredentialStore s = new OSXCredentialStore();

    s.storeCredentials(creds);

    TwitterCredentials creds2 = s.readDefaultCredentials();

    System.out.println(creds2);
  }
}
