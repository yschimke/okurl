package com.baulsupp.oksocial.services.facebook

import com.baulsupp.oksocial.authenticator.AuthUtil
import com.google.common.collect.Sets
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.*
import java.util.concurrent.CompletableFuture

object FacebookUtil {
    val VERSION = "v2.8"

    val API_HOSTS = setOf("graph.facebook.com")

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://graph.facebook.com" + s).build()
    }

    fun getMetadata(client: OkHttpClient, url: HttpUrl): CompletableFuture<FacebookMetadata> {
        var url = url
        url = url.newBuilder().addQueryParameter("metadata", "1").build()
        val request = Request.Builder().url(url).build()

        return AuthUtil.enqueueJsonMapRequest(client, request)
                .thenApply { m -> FacebookMetadata(m["metadata"] as Map<String, Any>) }
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
