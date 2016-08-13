package com.baulsupp.oksocial.security;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class ConsoleCallbackHandler implements CallbackHandler {
  @Override public void handle(Callback[] callbacks)
      throws IOException, UnsupportedCallbackException {

    for (Callback c : callbacks) {
      if (c instanceof PasswordCallback) {
        PasswordCallback pw = (PasswordCallback) c;
        pw.setPassword(System.console().readPassword(pw.getPrompt()));
      } else {
        throw new UnsupportedCallbackException(c);
      }
    }
  }
}
