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

data class MessageUser(val firstName: String?, val lastName: String?, val displayName: String, val userId: Long, val email: String, val username: String)
data class StreamMessageStream(val streamType: String, val streamId: String)
data class StreamMessage(val data: String, val stream: StreamMessageStream, val messageId: String, val userAgent: String,
                         val originalFormat: String, val message: String, val user: MessageUser, val timestamp: Long)

data class Signal(val name: String, val query: String, val visibleOnProfile: Boolean, val companyWide: Boolean, val id: String, val timestamp: Long)

data class MessageSent(val message: StreamMessage)
data class DatafeedInitiator(val user: MessageUser)
data class DatafeedStream(val streamType: String, val streamId: String)
data class DatafeedPayload(val messageSent: MessageSent)
data class DatafeedMessage(val payload: DatafeedPayload, val initiator: DatafeedInitiator, val messageId: String, val id: String, val type: String, val timestamp: Long)
