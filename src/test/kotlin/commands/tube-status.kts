#!/usr/bin/env okscript

import com.baulsupp.oksocial.kotlin.*
import com.squareup.moshi.Json
import kotlinx.coroutines.experimental.runBlocking
import org.fusesource.jansi.Ansi

data class StatusItem(
  val modeName: String,
  val lineStatuses: List<LineStatusesItem>?,
  val crowding: Crowding,
  val created: String,
  val name: String,
  val modified: String,
  val serviceTypes: List<ServiceTypesItem>?,
  val id: String,
  @Json(name = "\$type") val type: String
) {
  fun statusString(): String {
    return "%30s\t%s".format(this.name.color(statusColor()), this.lineStatuses?.sortedBy { it.statusSeverity }?.firstOrNull()?.statusSeverityDescription)
  }

  fun severity(): Int = lineStatuses?.map { it.statusSeverity }?.min() ?: 10

  fun statusColor() = when (severity()) {
    10 -> Ansi.Color.WHITE
    in 5..9 -> Ansi.Color.MAGENTA
    else -> Ansi.Color.RED
  }
}

data class LineStatusesItem(
  val statusSeverityDescription: String,
  val created: String,
  val statusSeverity: Int,
  val id: Int,
  @Json(name = "\$type") val type: String?
)

data class Crowding(@Json(name = "\$type") val type: String)

data class ServiceTypesItem(val name: String, val uri: String, @Json(name = "\$type") val type: String?)

runBlocking {
  val results = client.queryList<StatusItem>(
    "https://api.tfl.gov.uk/line/mode/tube/status")

  results.forEach {
    println(it.statusString())
  }
}
