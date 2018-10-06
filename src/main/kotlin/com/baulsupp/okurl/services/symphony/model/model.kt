package com.baulsupp.okurl.services.symphony.model

data class TokenResponse(val name: String, val token: String)

data class AvatarsItem(val size: String, val url: String)
data class SessionInfo(val emailAddress: String, val displayName: String, val roles: List<String>, val company: String,
                       val id: Long, val username: String, val avatars: List<AvatarsItem>)
