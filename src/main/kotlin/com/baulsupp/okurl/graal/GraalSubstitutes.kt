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
import com.oracle.svm.core.annotate.Delete
import com.oracle.svm.core.annotate.Substitute
import com.oracle.svm.core.annotate.TargetClass
import io.swagger.v3.parser.util.OpenAPIDeserializer
import okhttp3.internal.platform.Android10Platform
import okhttp3.internal.platform.AndroidPlatform
import okhttp3.internal.platform.BouncyCastlePlatform
import okhttp3.internal.platform.ConscryptPlatform
import okhttp3.internal.platform.Jdk8WithJettyBootPlatform
import okhttp3.internal.platform.Jdk9Platform
import okhttp3.internal.platform.OpenJSSEPlatform
import okhttp3.internal.platform.Platform
import java.util.Date

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

@TargetClass(AndroidPlatform::class)
@Delete
class TargetAndroidPlatform {
}

@TargetClass(Android10Platform::class)
@Delete
class TargetAndroid10Platform {
}

@TargetClass(BouncyCastlePlatform::class)
@Delete
class TargetBouncyCastlePlatform {
}

@TargetClass(ConscryptPlatform::class)
@Delete
class TargetConscryptPlatform {
}

@TargetClass(Jdk8WithJettyBootPlatform::class)
@Delete
class TargetJdk8WithJettyBootPlatform {
}

@TargetClass(OpenJSSEPlatform::class)
@Delete
class TargetOpenJSSEPlatform {
}

@TargetClass(Platform.Companion::class)
class TargetPlatform {
  @Substitute
  fun findPlatform(): Platform {
    return Jdk9Platform.buildIfSupported()!!
  }
}

@TargetClass(className = "java.util.JapaneseImperialCalendar")
@Delete()
class TargetJapaneseImperialCalendar {
}

@TargetClass(OpenAPIDeserializer::class)
class TargetOpenAPIDeserializer {
  @Substitute
  fun toDate(dateString: String): Date {
    return Date()
  }
}
