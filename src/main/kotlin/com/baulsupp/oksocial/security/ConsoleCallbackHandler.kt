package com.baulsupp.oksocial.security

import javax.security.auth.callback.Callback
import javax.security.auth.callback.CallbackHandler
import javax.security.auth.callback.PasswordCallback
import javax.security.auth.callback.UnsupportedCallbackException

class ConsoleCallbackHandler : CallbackHandler {
  override fun handle(callbacks: Array<Callback>) {
    for (c in callbacks) {
      if (c is PasswordCallback) {
        c.password = System.console().readPassword(c.prompt)
      } else {
        throw UnsupportedCallbackException(c)
      }
    }
  }
}
