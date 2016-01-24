package com.baulsupp.oksocial.credentials;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okio.BufferedSource;
import okio.Okio;

public abstract class OSXCredentialsStore<T> implements CredentialsStore<T> {
  public abstract String apiHost();

  public abstract String serviceName();

  @Override public T readDefaultCredentials() throws IOException {
    Process process =
        new ProcessBuilder("/usr/bin/security", "find-generic-password", "-a", apiHost(),
            "-D", "oauth credentials", "-w")
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start();

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

  @Override public void storeCredentials(T credentials) throws IOException {
    String credentialsString = formatCredentialsString(credentials);

    Process process =
        new ProcessBuilder("/usr/bin/security", "add-generic-password", "-a", apiHost(),
            "-D", "oauth credentials", "-s", serviceName(), "-U", "-w",
            credentialsString)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start();

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

  public abstract T parseCredentialsString(String s);

  public abstract String formatCredentialsString(T credentials);
}
