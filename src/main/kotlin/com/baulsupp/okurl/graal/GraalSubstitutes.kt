package com.baulsupp.okurl.graal

import com.baulsupp.oksocial.output.ConsoleHandler
import com.baulsupp.okurl.credentials.CredentialFactory
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.SimpleCredentialsStore
import com.oracle.svm.core.annotate.Substitute
import com.oracle.svm.core.annotate.TargetClass

@TargetClass(ConsoleHandler::class)
class TargetConsoleHandler {
  @Substitute
  suspend fun openLink(url: String) {
    System.err.println(url)
  }
}

@TargetClass(CredentialFactory::class)
class TargetCredentialFactory {
  @Substitute
  fun createCredentialsStore(): CredentialsStore {
    return SimpleCredentialsStore
  }
}
