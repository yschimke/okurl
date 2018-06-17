#!/usr/bin/env okscript

import com.baulsupp.oksocial.kotlin.*
import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.oksocial.services.mapbox.model.MapboxDrivingResults
import com.baulsupp.oksocial.services.mapbox.model.MapboxPlacesResult
import com.baulsupp.oksocial.services.mapbox.staticMap
import com.baulsupp.oksocial.services.uber.model.UberPriceEstimates
import com.baulsupp.oksocial.services.uber.model.UberTimeEstimates
import com.baulsupp.oksocial.util.ClientException
import kotlinx.coroutines.experimental.runBlocking
import kotlin.system.exitProcess

if (args.isEmpty())
  throw UsageException("usage: uberprices Destination")

runBlocking {
  warmup("https://api.mapbox.com/robots.txt", "https://api.uber.com/robots.txt")

  val loc = location()

  if (loc == null) {
    System.err.println("no current location")
    return@runBlocking
  }

  val possibleDestinations = client.query<MapboxPlacesResult>("https://api.mapbox.com/geocoding/v5/mapbox.places/${args.joinToString("+")}.json?proximity=${loc.longitude},${loc.latitude}")

  val firstDestination = possibleDestinations.features.getOrNull(0)

  if (firstDestination == null) {
    System.err.println("no results for '${args.joinToString(" ")}'")
    exitProcess(-2)
  }

  println(firstDestination.text)

  val dest = firstDestination.center

  val drivingRoute = try {
    client.query<MapboxDrivingResults>("https://api.mapbox.com/directions/v5/mapbox/driving/${loc.longitude},${loc.latitude};${dest.longitude},${dest.latitude}?overview=full&geometries=polyline")
  } catch (e: ClientException) {
    null
  }

  show(staticMap {
    pinLocation(loc)
    route(drivingRoute?.routes?.firstOrNull()?.geometry)
    pinLocation(dest)
  })

  val prices = client.query<UberPriceEstimates>("https://api.uber.com/v1.2/estimates/price?start_latitude=${loc.latitude}&start_longitude=${loc.longitude}&end_latitude=${dest.latitude}&end_longitude=${dest.longitude}").prices
  val times = client.query<UberTimeEstimates>("https://api.uber.com/v1.2/estimates/time?start_latitude=${loc.latitude}&start_longitude=${loc.longitude}&end_latitude=${dest.latitude}&end_longitude=${dest.longitude}").times

  for (price in prices) {
    val time = times.find { it.productId == price.productId }
    val timeEstimate = time?.estimate?.let { "${it / 60} min" } ?: "Unknown"
    println(price.localizedDisplayName.padEnd(15) + "\t" + timeEstimate.padEnd(15) + "\t" + price.estimate)
  }
}
