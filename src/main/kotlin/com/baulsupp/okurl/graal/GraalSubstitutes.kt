package com.baulsupp.okurl.graal

import com.baulsupp.oksocial.output.ConsoleHandler
import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.oksocial.output.isOSX
import com.baulsupp.oksocial.output.process.exec
import com.baulsupp.oksocial.output.stdErrLogging
import com.baulsupp.okurl.Main
import com.baulsupp.okurl.credentials.CredentialFactory
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.SimpleCredentialsStore
import com.oracle.svm.core.annotate.Substitute
import com.oracle.svm.core.annotate.TargetClass

@TargetClass(ConsoleHandler::class)
class TargetConsoleHandler {
  @Substitute
  suspend fun openLink(url: String) {
    if (isOSX) {
      val result = exec(listOf("open", url)) {
        readOutput(true)
        redirectError(stdErrLogging)
      }

      if (!result.success) {
        throw UsageException("open url failed: $url")
      }
    } else {
      System.err.println(url)
    }
  }
}

@TargetClass(CredentialFactory::class)
class TargetCredentialFactory {
  @Substitute
  fun createCredentialsStore(): CredentialsStore {
    return SimpleCredentialsStore
  }
}

@TargetClass(Main.Companion::class)
class TargetMain {
  @Substitute
  fun setupProvider() {
    throw IllegalArgumentException("--conscrypt unsupported with graal")
  }
}
