package com.baulsupp.oksocial.credentials;

import com.google.common.base.Throwables;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import okio.BufferedSource;
import okio.Okio;

public class OSXCredentialsStore implements CredentialsStore {
  private final Optional<String> tokenSet;

  public OSXCredentialsStore(Optional<String> tokenSet) {
    this.tokenSet = tokenSet;
  }

  public OSXCredentialsStore() {
    this(Optional.empty());
  }

  @Override public <T> Optional<T> readDefaultCredentials(ServiceDefinition<T> serviceDefinition) {
    try {
      Process process =
          new ProcessBuilder("/usr/bin/security", "find-generic-password", "-a",
              serviceDefinition.apiHost(),
              "-D", tokenKey(), "-w")
              .redirectError(new File("/dev/null"))
              .start();

      try {
        process.getOutputStream().close();
        // may timeout if password is somehow longer than the pipe can hold
        if (!process.waitFor(10, TimeUnit.SECONDS)) {
          process.destroyForcibly();
          throw new IOException("timeout calling /usr/bin/security");
        }

        if (process.exitValue() == 44) {
          return Optional.empty();
        }

        BufferedSource stderr = Okio.buffer(Okio.source(process.getErrorStream()));
        String errorLog = stderr.readString(Charset.defaultCharset());

        if (errorLog != null) {
          System.err.print(errorLog);
        }

        if (process.exitValue() != 0) {
          throw new IOException("/usr/bin/security exited with return code " + process.exitValue());
        }

        BufferedSource stdout = Okio.buffer(Okio.source(process.getInputStream()));

        return Optional.ofNullable(
            serviceDefinition.parseCredentialsString(stdout.readUtf8LineStrict()));
      } catch (InterruptedException e) {
        throw new IOException(e);
      } finally {
        if (process.isAlive()) {
          process.destroyForcibly();
        }
      }
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public <T> void storeCredentials(T credentials, ServiceDefinition<T> serviceDefinition) {
    try {
      String credentialsString = serviceDefinition.formatCredentialsString(credentials);

      Process process =
          new ProcessBuilder("/usr/bin/security", "add-generic-password", "-a",
              serviceDefinition.apiHost(),
              "-D", tokenKey(), "-s", serviceDefinition.serviceName(), "-U", "-w",
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
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  private String tokenKey() {
    return "oauth" + tokenSet.map(s -> "." + s).orElse("");
  }
}
