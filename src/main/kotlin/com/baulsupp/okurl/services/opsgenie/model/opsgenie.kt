package com.baulsupp.okurl.services.opsgenie.model

data class Role(
  val name: String,
  val id: String
)

data class User(
  val userAddress: UserAddress,
  val createdAt: String?,
  val role: Role?,
  val blocked: Boolean?,
  val verified: Boolean?,
  val fullName: String,
  val timeZone: String?,
  val id: String,
  val locale: String?,
  val username: String
)

data class Paging(
  val last: String?,
  val first: String?,
  val next: String?
)

data class UserAddress(
  val country: String?,
  val zipCode: String?,
  val city: String?,
  val line: String?,
  val state: String?
)

data class UsersResponse(
  val took: Double,
  val data: List<User>,
  val requestId: String,
  val paging: Paging?,
  val totalCount: Int
)

data class Account(
  val userCount: Int,
  val name: String,
  val plan: Plan?
)

data class AccountResponse(
  val took: Double,
  val data: Account,
  val requestId: String
)

data class Plan(
  val isYearly: Boolean,
  val name: String,
  val maxUserCount: Int
)

data class Team(val name: String?, val description: String?, val id: String)

data class TeamsResponse(val took: Double, val data: List<Team>, val requestId: String)

data class Schedule(
  val timezone: String,
  val name: String,
  val description: String,
  val id: String,
  val ownerTeam: Team,
  val enabled: Boolean
)

data class SchedulesResponse(
  val took: Double,
  val data: List<Schedule>,
  val expandable: List<String>?,
  val requestId: String
)

data class Integration(
  val name: String,
  val id: String,
  val type: String
)

data class AlertsResponse(
  val took: Double,
  val data: List<Alert>,
  val requestId: String,
  val paging: Paging
)

data class AlertResponse(
  val took: Double,
  val data: Alert,
  val requestId: String
)

data class Report(
  val closedBy: String?,
  val ackTime: Int?,
  val closeTime: Int?
)

data class Alert(
  val owner: String?,
  val acknowledged: Boolean?,
  val teams: List<Team>?,
  val count: Int,
  val source: String?,
  val message: String,
  val description: String?,
  val snoozed: Boolean,
  val priority: String,
  val isSeen: Boolean,
  val responders: List<Responder>?,
  val seen: Boolean,
  val createdAt: String,
  val tinyId: String,
  val integration: Integration?,
  val report: Report?,
  val alias: String,
  val id: String,
  val status: String,
  val lastOccurredAt: String,
  val updatedAt: String
)

data class Responder(
  val id: String
)
