package com.baulsupp.okurl.services.trello.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TokenResponse(
  val identifier: String,
  val dateCreated: String,
  val permissions: List<Permission>,
  val idMember: String,
  val dateExpires: String?,
  val id: String
)

@JsonClass(generateAdapter = true)
data class Permission(
  val read: Boolean,
  val modelType: String,
  val idModel: String,
  val write: Boolean
)

@JsonClass(generateAdapter = true)
data class MemberResponse(
  val bio: String?,
  val avatarSource: String?,
  val idBoardsPinned: Any?,
  val confirmed: Boolean?,
  val uploadedAvatarHash: Any?,
  val id: String,
  val bioData: Any?,
  val email: String,
  val limits: Limits?,
  val uploadedAvatarUrl: String?,
  val avatarUrl: String?,
  val initials: String,
  val fullName: String,
  val loginTypes: List<String>,
  val url: String,
  val marketingOptIn: MarketingOptIn?,
  val prefs: Map<String, Any>,
  val gravatarHash: String,
  val avatarHash: String,
  val idEnterprise: Any?,
  val memberType: String,
  val idBoards: List<String>?,
  val status: String,
  val username: String
)

@JsonClass(generateAdapter = true)
data class MarketingOptIn(
  val date: String,
  val optedIn: Boolean
)

@JsonClass(generateAdapter = true)
data class Limits(
  val boards: Boards?,
  val orgs: Orgs?
)

@JsonClass(generateAdapter = true)
data class Boards(val totalPerMember: TotalPerMember?)

@JsonClass(generateAdapter = true)
data class Orgs(val totalPerMember: TotalPerMember?)

@JsonClass(generateAdapter = true)
data class TotalPerMember(
  val warnAt: Int,
  val disableAt: Int,
  val status: String
)

@JsonClass(generateAdapter = true)
data class BoardResponse(
  val descData: Any?,
  val pinned: Any?,
  val labelNames: Map<String, String>,
  val shortUrl: String,
  val invited: Boolean?,
  val dateLastActivity: String?,
  val datePluginDisable: Any?,
  val shortLink: String,
  val url: String,
  val memberships: List<Any>,
  val prefs: Map<String, Any>,
  val subscribed: Boolean,
  val starred: Boolean,
  val invitations: Any?,
  val name: String,
  val idOrganization: Any?,
  val dateLastView: String?,
  val closed: Boolean,
  val id: String,
  val limits: Any?,
  val desc: String?
)
