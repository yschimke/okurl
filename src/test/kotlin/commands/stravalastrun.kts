#!/usr/bin/env okscript

import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.okurl.kotlin.client
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.queryList
import com.baulsupp.okurl.kotlin.show
import com.baulsupp.okurl.kotlin.warmup
import com.baulsupp.okurl.services.mapbox.StaticMapBuilder
import com.baulsupp.okurl.services.mapbox.staticMap
import com.baulsupp.okurl.services.strava.model.ActivitySummary
import kotlinx.coroutines.runBlocking

fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)

runBlocking {
  warmup("https://api.mapbox.com/robots.txt", "https://www.strava.com/robots.txt")

  val activities =
    client.queryList<ActivitySummary>("https://www.strava.com/api/v3/athlete/activities?page=1&per_page=1")

  if (activities.isEmpty()) {
    throw UsageException("No activities found")
  }

  val lastActivity = client.query<ActivitySummary>("https://www.strava.com/api/v3/activities/${activities.first().id}")

  show(staticMap {
    style = StaticMapBuilder.Satellite
    route(lastActivity.map.polyline)
  })

  println("https://www.strava.com/activities/" + lastActivity.id)
  println("Distance: ${(lastActivity.distance / 1000.0).format(1)} km")
  println("Duration: ${(lastActivity.elapsed_time / 60.0).format(0)} minutes")
}
