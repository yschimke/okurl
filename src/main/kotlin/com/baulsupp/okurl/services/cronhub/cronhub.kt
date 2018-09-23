package com.baulsupp.okurl.services.cronhub

import com.baulsupp.okurl.kotlin.client
import com.baulsupp.okurl.kotlin.query

data class MonitorsResponse(
  val success: Boolean,
  val response: List<ResponseItem>
)

data class EventTime(
  val date: String,
  val timezone: String,
  val timezone_type: Int
)

data class ResponseItem(
  val schedule: String,
  val code: String,
  val last_ping: EventTime?,
  val timezone: String,
  val name: String,
  val running_time: String?,
  val created_at: EventTime,
  val running_time_unit: String?,
  val grace_period: Int,
  val status: String
)

data class MonitorStatus(
  val success: Boolean,
  val monitor_status: String,
  val monitor_last_ping: EventTime?
)

suspend fun monitor(uuid: String, block: () -> Unit) {
  client.query<MonitorStatus>("https://cronhub.io/start/$uuid")

  try {
    block()
  } finally {
    client.query<MonitorStatus>("https://cronhub.io/finish/$uuid")
  }
}

suspend fun ping(uuid: String): MonitorStatus {
  return client.query("https://cronhub.io/ping/$uuid")
}
