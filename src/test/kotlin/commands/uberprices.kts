#!/usr/bin/env okscript

import com.baulsupp.oksocial.kotlin.*
import com.baulsupp.oksocial.location.Location
import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.oksocial.services.mapbox.model.MapboxDrivingResults
import com.baulsupp.oksocial.services.mapbox.model.MapboxPlacesResult
import com.baulsupp.oksocial.services.uber.model.UberPriceEstimates
import com.baulsupp.oksocial.services.uber.model.UberTimeEstimates
import com.baulsupp.oksocial.util.ClientException
import java.net.URLEncoder
import kotlin.system.exitProcess

fun staticMap(start: Location, dest: Location? = null, route: String? = null): String {
  val markers = mutableListOf<String>();
  markers.add("pin-m-marker+CCC(" + start.longitude + "," + start.latitude + ")");

  if (dest != null) {
    markers.add("pin-m-marker+CCC(" + dest.longitude + "," + dest.latitude + ")");
  }

  if (route != null) {
    markers.add("path-2+f44-0.75+f44-0.0(${URLEncoder.encode(route, "UTF-8")})");
  }

  return "https://api.mapbox.com/v4/mapbox.dark/${markers.joinToString(",")}/auto/400x400.png";
}

fun uberResults(vararg args: String) {
  warmup("https://api.mapbox.com/robots.txt", "https://api.uber.com/robots.txt")

  val loc = location()

  if (loc == null) {
    System.err.println("no current location")
    return
  }

  val possibleDestinations = query<MapboxPlacesResult>("https://api.mapbox.com/geocoding/v5/mapbox.places/${args.joinToString("+")}.json?proximity=${loc.longitude},${loc.latitude}")

  val firstDestination = possibleDestinations.features.getOrNull(0)

  if (firstDestination == null) {
    System.err.println("no results for '${args.joinToString(" ")}'")
    exitProcess(-2)
  }

  println(firstDestination.text)

  val dest = firstDestination.center

  val route = try {
    query<MapboxDrivingResults>("https://api.mapbox.com/directions/v5/mapbox/driving/${loc.longitude},${loc.latitude};${dest.longitude},${dest.latitude}?overview=full&geometries=polyline")
  } catch (e: ClientException) {
    null
  }

  show(staticMap(loc, dest, route?.routes?.firstOrNull()?.geometry))

  val prices = query<UberPriceEstimates>("https://api.uber.com/v1.2/estimates/price?start_latitude=${loc.latitude}&start_longitude=${loc.longitude}&end_latitude=${dest.latitude}&end_longitude=${dest.longitude}")
  val times = query<UberTimeEstimates>("https://api.uber.com/v1.2/estimates/time?start_latitude=${loc.latitude}&start_longitude=${loc.longitude}&end_latitude=${dest.latitude}&end_longitude=${dest.longitude}")

  for (price in prices.prices) {
    val time = times.times.find { it.productId == price.productId }

    val timeEstimate = time?.estimate?.let { "${it / 60} min" } ?: "Unknown"
    println(price.localizedDisplayName.padEnd(15) + "\t" + timeEstimate.padEnd(15) + "\t" + price.estimate);
  }
}

if (args.isEmpty())
  throw UsageException("usage: uberprices Destination")

val b = args.toList()

uberResults(*b.toTypedArray())
