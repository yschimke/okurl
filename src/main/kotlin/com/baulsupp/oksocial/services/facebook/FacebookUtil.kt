package com.baulsupp.oksocial.services.facebook

import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.services.facebook.model.Metadata
import com.baulsupp.oksocial.services.facebook.model.MetadataResult
import com.baulsupp.oksocial.util.ClientException
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

object FacebookUtil {
  val VERSION = "v2.11"

  val API_HOSTS = setOf("graph.facebook.com", "www.facebook.com")

  fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
    return requestBuilder.url("https://graph.facebook.com" + s).build()
  }

  suspend fun getMetadata(client: OkHttpClient, url: HttpUrl): Metadata? {
    val newUrl = url.newBuilder().addQueryParameter("metadata", "1").build()
    val request = Request.Builder().url(newUrl).build()

    return try {
      val response = client.query<MetadataResult>(request)
      response.metadata
    } catch (ce: ClientException) {
      if (ce.code != 404) {
        throw ce
      }
      null
    }
  }

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
}
