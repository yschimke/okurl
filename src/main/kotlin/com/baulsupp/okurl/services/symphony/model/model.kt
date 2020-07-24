package com.baulsupp.okurl.services.symphony.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TokenResponse(val name: String, val token: String)

@JsonClass(generateAdapter = true)
data class AvatarsItem(val size: String, val url: String)

@JsonClass(generateAdapter = true)
data class SessionInfo(
  val emailAddress: String,
  val displayName: String,
  val roles: List<String>,
  val company: String,
  val id: Long,
  val username: String,
  val avatars: List<AvatarsItem>
)

@JsonClass(generateAdapter = true)
data class StreamListRequest(val streamTypes: List<StreamType>) {
  constructor(vararg streamTypes: StreamType) : this(streamTypes.toList())
  constructor(vararg streamTypes: String) : this(streamTypes.map(::StreamType))
}

@JsonClass(generateAdapter = true)
data class StreamType(val type: String)

@JsonClass(generateAdapter = true)
data class StreamAttributes(val members: List<Long>)

@JsonClass(generateAdapter = true)
data class Stream(
  val id: String,
  val crossPod: Boolean,
  val active: Boolean,
  val streamType: StreamType,
  val streamAttributes: StreamAttributes?
)

@JsonClass(generateAdapter = true)
data class MessageUser(
  val firstName: String?,
  val lastName: String?,
  val displayName: String,
  val userId: Long,
  val email: String,
  val username: String
)

@JsonClass(generateAdapter = true)
data class StreamMessageStream(val streamType: String, val streamId: String)

@JsonClass(generateAdapter = true)
data class StreamMessage(
  val data: String,
  val stream: StreamMessageStream,
  val messageId: String,
  val userAgent: String,
  val originalFormat: String,
  val message: String,
  val user: MessageUser,
  val timestamp: Long
)

@JsonClass(generateAdapter = true)
data class Signal(
  val name: String,
  val query: String,
  val visibleOnProfile: Boolean,
  val companyWide: Boolean,
  val id: String,
  val timestamp: Long
)

@JsonClass(generateAdapter = true)
data class MessageSent(val message: StreamMessage)

@JsonClass(generateAdapter = true)
data class DatafeedInitiator(val user: MessageUser)

@JsonClass(generateAdapter = true)
data class DatafeedStream(val streamType: String, val streamId: String)

@JsonClass(generateAdapter = true)
data class DatafeedPayload(val messageSent: MessageSent)

@JsonClass(generateAdapter = true)
data class DatafeedMessage(
  val payload: DatafeedPayload,
  val initiator: DatafeedInitiator,
  val messageId: String,
  val id: String,
  val type: String,
  val timestamp: Long
)
