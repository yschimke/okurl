package com.baulsupp.okurl.security

import javax.security.auth.callback.Callback
import javax.security.auth.callback.CallbackHandler
import javax.security.auth.callback.PasswordCallback
import javax.security.auth.callback.UnsupportedCallbackException

object ConsoleCallbackHandler : CallbackHandler {
  override fun handle(callbacks: Array<Callback>) {
    for (callback in callbacks) {
      if (callback is PasswordCallback) {
        val console = System.console()

        if (console != null) {
          callback.password = console.readPassword(callback.prompt)
        } else {
          System.err.println(callback.prompt)
          callback.password = System.`in`.bufferedReader().readLine().toCharArray()
        }
      } else {
        throw UnsupportedCallbackException(callback)
      }
    }
  }
}
