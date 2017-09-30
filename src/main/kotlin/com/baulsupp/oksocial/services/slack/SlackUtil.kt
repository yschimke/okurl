package com.baulsupp.oksocial.services.slack

import okhttp3.Request
import java.util.*

object SlackUtil {
    val SCOPES: Collection<String> = Arrays.asList("channels:history",
            "channels:read",
            "channels:write",
            "chat:write:bot",
            "chat:write:user",
            "dnd:read",
            "dnd:write",
            "emoji:read",
            "files:read",
            "files:write:user",
            "groups:history",
            "groups:read",
            "groups:write",
            "im:history",
            "im:read",
            "im:write",
            "mpim:history",
            "mpim:read",
            "mpim:write",
            "pins:read",
            "pins:write",
            "reactions:read",
            "reactions:write",
            "reminders:read",
            "reminders:write",
            "search:read",
            "stars:read",
            "stars:write",
            "team:read",
            "usergroups:read",
            "usergroups:write",
            "users.profile:read",
            "users.profile:write",
            "users:read",
            "users:write")

    val API_HOSTS = setOf((
            "slack.com")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://slack.com" + s).build()
    }
}
