package com.baulsupp.okurl.services.symphony.model

data class TokenResponse(val name: String, val token: String)

data class AvatarsItem(val size: String, val url: String)
data class SessionInfo(val emailAddress: String, val displayName: String, val roles: List<String>, val company: String,
                       val id: Long, val username: String, val avatars: List<AvatarsItem>)

data class StreamListRequest(val streamTypes: List<StreamType>) {
  constructor(vararg streamTypes: StreamType) : this(streamTypes.toList())
  constructor(vararg streamTypes: String) : this(streamTypes.map(::StreamType))
}

data class StreamType(val type: String)
data class StreamAttributes(val members: List<Long>)
data class Stream(val id: String, val crossPod: Boolean, val active: Boolean, val streamType: StreamType, val streamAttributes: StreamAttributes?)

data class StreamMessageUser(val displayName: String, val userId: Long, val email: String, val username: String)
data class StreamMessageStream(val streamType: String, val streamId: String)
data class StreamMessage(val data: String, val stream: StreamMessageStream, val messageId: String, val userAgent: String,
                         val originalFormat: String, val message: String, val user: StreamMessageUser, val timestamp: Long)
