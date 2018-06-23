package com.baulsupp.oksocial.services.trello

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Response
import java.net.URLEncoder

object TrelloAuthFlow {
  suspend fun login(
    outputHandler: OutputHandler<Response>
  ): TrelloToken {
    val clientKey = Secrets.prompt("Trello API Key", "trello.apiKey", "", false)

    val scopes = Secrets.promptArray("Scopes", "trello.scopes", listOf("read", "write", "account"))
    val scopesString = URLEncoder.encode(scopes.joinToString(","), "UTF-8")

    val loginUrl = "https://trello.com/1/authorize?expiration=never&name=OkSocial&scope=$scopesString&key=$clientKey&response_type=token"

    outputHandler.openLink(loginUrl)

    val token = System.console().readLine("Enter Token: ")

    return TrelloToken(token, clientKey)
  }
}
