package com.baulsupp.okurl.graal

import com.baulsupp.okurl.Main
import com.baulsupp.okurl.credentials.CredentialFactory
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.SimpleCredentialsStore
import com.oracle.svm.core.annotate.Delete
import com.oracle.svm.core.annotate.Substitute
import com.oracle.svm.core.annotate.TargetClass
import io.swagger.v3.parser.util.OpenAPIDeserializer
import java.util.Date

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

@TargetClass(OpenAPIDeserializer::class)
class TargetOpenAPIDeserializer {
  @Substitute
  fun toDate(dateString: String): Date {
    return Date()
  }
}

