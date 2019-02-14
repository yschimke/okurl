package com.baulsupp.okurl.authenticator.oauth2

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.authenticator.authflow.AuthFlow
import com.baulsupp.okurl.authenticator.authflow.Callback
import com.baulsupp.okurl.authenticator.authflow.Prompt
import com.baulsupp.okurl.authenticator.authflow.Scopes
import com.baulsupp.okurl.authenticator.authflow.State
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.OkHttpClient
import java.util.UUID

object Oauth2DesktopFlow {
  suspend fun login(
    authFlow: Oauth2Flow<Oauth2Token>,
    client: OkHttpClient,
    outputHandler: OutputHandler<*>
  ): Oauth2Token {
    return SimpleWebServer.forCode().use { s ->
      val state = UUID.randomUUID().toString()
      authFlow.init(client)

      val options = authFlow.options()

      val params = options.map {
        val value: Any = when (it) {
          is Prompt -> Secrets.prompt(it.label, it.param, it.default ?: "", it.secret)
          is Scopes -> Secrets.promptArray("Scopes", it.param, it.default ?: it.known)
          is Callback -> s.redirectUri
          is State -> state
        }

        it.param to value
      }.toMap()

      authFlow.defineOptions(params)
      val url = authFlow.start()

      outputHandler.openLink(url)
      val code = s.waitForCode()

      authFlow.complete(code)
    }
  }
}
