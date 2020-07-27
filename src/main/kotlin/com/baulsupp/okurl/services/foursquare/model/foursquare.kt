package com.baulsupp.okurl.services.foursquare.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Meta(
  val code: Int,
  val requestId: String
)

@JsonClass(generateAdapter = true)
data class Todo(val count: Int)

@JsonClass(generateAdapter = true)
data class User(
  val birthday: Int,
  val lastName: String,
  val blockedStatus: String,
  val gender: String,
  val bio: String,
  val requests: Requests,
  val type: String,
  val photos: Photos,
  val tips: Tips,
  val createdAt: Int,
  val homeCity: String,
  val contact: Any,
  val pings: Boolean,
  val id: String,
  val relationship: String,
  val canonicalUrl: String,
  val referralId: String,
  val photo: Photo,
  val mayorships: Mayorships,
  val friends: Friends,
  val firstName: String,
  val checkinPings: String,
  val lists: Lists,
  val checkins: Checkins
)

@JsonClass(generateAdapter = true)
data class Friends(
  val count: Int,
  val groups: List<GroupsItem>?
)

@JsonClass(generateAdapter = true)
data class CategoriesItem(
  val pluralName: String,
  val name: String,
  val icon: Icon,
  val id: String,
  val shortName: String,
  val primary: Boolean
)

@JsonClass(generateAdapter = true)
data class ItemsItem(
  val venue: Venue,
  val private: Boolean,
  val comments: Comments,
  val visibility: String,
  val like: Boolean,
  val timeZoneOffset: Int,
  val isMayor: Boolean,
  val source: Source,
  val type: String,
  val photos: Photos,
  val posts: Posts,
  val createdAt: Int,
  val id: String,
  val likes: Likes
)

@JsonClass(generateAdapter = true)
data class Photo(
  val prefix: String,
  val suffix: String
)

@JsonClass(generateAdapter = true)
data class SelfResponse(
  val meta: Meta,
  val response: Response,
  val notifications: List<NotificationsItem>?
)

@JsonClass(generateAdapter = true)
data class Requests(val count: Int)

@JsonClass(generateAdapter = true)
data class Source(
  val name: String,
  val url: String
)

@JsonClass(generateAdapter = true)
data class Photos(val count: Int)

@JsonClass(generateAdapter = true)
data class Item(val unreadCount: Int)

@JsonClass(generateAdapter = true)
data class Lists(
  val count: Int,
  val groups: List<GroupsItem>?
)

@JsonClass(generateAdapter = true)
data class LabeledLatLngsItem(
  val lng: Double,
  val label: String,
  val lat: Double
)

@JsonClass(generateAdapter = true)
data class Mayorships(val count: Int)

@JsonClass(generateAdapter = true)
data class NotificationsItem(
  val item: Item,
  val type: String
)

@JsonClass(generateAdapter = true)
data class Venue(
  val stats: Stats,
  val like: Boolean,
  val contact: Any,
  val name: String,
  val verified: Boolean,
  val ratedAt: Int,
  val location: Location,
  val id: String,
  val categories: List<CategoriesItem>?,
  val allowMenuUrlEdit: Boolean?,
  val beenHere: BeenHere
)

@JsonClass(generateAdapter = true)
data class Comments(val count: Int)

@JsonClass(generateAdapter = true)
data class Posts(
  val count: Int,
  val textCount: Int
)

@JsonClass(generateAdapter = true)
data class GroupsItem(
  val count: Int,
  val type: String
)

@JsonClass(generateAdapter = true)
data class BeenHere(
  val lastCheckinExpiredAt: Int
)

@JsonClass(generateAdapter = true)
data class Response(val user: User)

@JsonClass(generateAdapter = true)
data class Checkins(
  val count: Int,
  val items: List<ItemsItem>?
)

@JsonClass(generateAdapter = true)
data class Stats(
  val checkinsCount: Int,
  val usersCount: Int,
  val tipCount: Int
)

@JsonClass(generateAdapter = true)
data class Tip(
  val todo: Todo,
  val createdAt: Int,
  val canonicalUrl: String,
  val like: Boolean,
  val agreeCount: Int,
  val id: String,
  val text: String,
  val viewCount: Int,
  val type: String,
  val disagreeCount: Int,
  val likes: Likes
)

@JsonClass(generateAdapter = true)
data class Icon(
  val prefix: String,
  val suffix: String
)

@JsonClass(generateAdapter = true)
data class Likes(val count: Int)

@JsonClass(generateAdapter = true)
data class Location(
  val cc: String,
  val country: String,
  val address: String,
  val labeledLatLngs: List<LabeledLatLngsItem>?,
  val lng: Double,
  val formattedAddress: List<String>?,
  val city: String,
  val state: String,
  val lat: Double
)

@JsonClass(generateAdapter = true)
data class Tips(val count: Int)
