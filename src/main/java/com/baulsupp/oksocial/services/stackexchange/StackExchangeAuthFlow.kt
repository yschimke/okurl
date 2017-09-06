package com.baulsupp.oksocial.services.stackexchange

import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.output.OutputHandler
import java.io.IOException
import java.net.URLEncoder
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

import com.baulsupp.oksocial.authenticator.AuthUtil.makeSimpleRequest
import java.util.stream.Collectors.joining

object StackExchangeAuthFlow {

    @Throws(IOException::class)
    fun login(client: OkHttpClient, outputHandler: OutputHandler<*>,
              clientId: String, clientSecret: String, clientKey: String, scopes: Set<String>): StackExchangeToken {
        SimpleWebServer.forCode().use { s ->

            val serverUri = s.redirectUri

            val loginUrl = "https://stackexchange.com/oauth"
            +"?client_id=" + clientId
            +"&redirect_uri=" + serverUri
            +"&scope=" + URLEncoder.encode(scopes.stream().collect<String, *>(joining(",")),
                    "UTF-8")

            outputHandler.openLink(loginUrl)

            val code = s.waitForCode()

            val tokenUrl = "https://stackexchange.com/oauth/access_token"

            val body = FormBody.Builder().add("client_id", clientId).add("redirect_uri", serverUri)
                    .add("client_secret", clientSecret).add("code", code).build()
            val request = Request.Builder().url(tokenUrl).method("POST", body).build()

            val longTokenBody = makeSimpleRequest(client, request)

            return StackExchangeToken(parseExchangeRequest(longTokenBody), clientKey)
        }
    }

    private fun parseExchangeRequest(body: String): String? {
        val params = body.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        for (p in params) {
            val parts = p.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            if (parts[0] == "access_token") {
                return parts[1]
            }
        }

        return null
    }
}
