package com.baulsupp.okurl.services.facebook

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.NoToken
import com.baulsupp.okurl.kotlin.queryMap
import okhttp3.OkHttpClient
import okhttp3.Response
import java.net.URLEncoder

object FacebookAuthFlow {
  val ALL_PERMISSIONS = listOf(
    "public_profile",
    "user_friends",
    "email",
    "user_about_me",
    "user_actions.books",
    "user_actions.fitness",
    "user_actions.music",
    "user_actions.news",
    "user_actions.video",
    "user_birthday",
    "user_education_history",
    "user_events",
    "user_games_activity",
    "user_hometown",
    "user_likes",
    "user_location",
    "user_managed_groups",
    "user_photos",
    "user_posts",
    "user_relationships",
    "user_relationship_details",
    "user_religion_politics",
    "user_tagged_places",
    "user_videos",
    "user_website",
    "user_work_history",
    "read_custom_friendlists",
    "read_insights",
    "read_audience_network_insights",
    "read_page_mailboxes",
    "manage_pages",
    "publish_pages",
    "publish_actions",
    "rsvp_event",
    "pages_show_list",
    "pages_manage_cta",
    "pages_manage_instant_articles",
    "ads_read",
    "ads_management",
    "pages_messaging",
    "pages_messaging_phone_number"
  )

  suspend fun login(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    clientId: String,
    clientSecret: String,
    scopes: List<String>
  ): Oauth2Token {
    SimpleWebServer.forCode().use { s ->

      val serverUri = s.redirectUri

      val loginUrl =
        "https://www.facebook.com/dialog/oauth?client_id=$clientId&redirect_uri=$serverUri&scope=" + URLEncoder.encode(
          scopes.joinToString(","), "UTF-8"
        )

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val tokenUrl =
        "https://graph.facebook.com/v2.10/oauth/access_token?client_id=$clientId&redirect_uri=$serverUri&client_secret=$clientSecret&code=$code"

      val map = client.queryMap<Any>(tokenUrl, NoToken)

      val shortToken = map["access_token"] as String

      val exchangeUrl =
        "https://graph.facebook.com/oauth/access_token?grant_type=fb_exchange_token&client_id=$clientId&client_secret=$clientSecret&fb_exchange_token=$shortToken"

      val longTokenBody = client.queryMap<Any>(
        exchangeUrl,
        NoToken
      )

      return Oauth2Token(longTokenBody["access_token"] as String, "", clientId, clientSecret)
    }
  }
}
