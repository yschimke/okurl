#!/usr/bin/env okscript

import com.baulsupp.oksocial.kotlin.*
import com.baulsupp.oksocial.location.Location
import com.baulsupp.oksocial.services.datasettes.model.DatasetteResultSet

fun queryCity(city: String) = query<DatasetteResultSet>("https://nhs-england-hospitals.now.sh/hospitals-0cda400.json?sql=select+OrganisationName%2C+Latitude%2C+Longitude%2C+Phone+from+hospitals+where+City+%3D+%27${city}%27")

fun staticMap(start: Location, hospitals: List<Location>): String {
  val markers = mutableListOf<String>();
  markers.add("pin-m-marker+CCC(" + start.longitude + "," + start.latitude + ")");

  hospitals.forEach {
    markers.add("pin-s-hospital(" + it.longitude + "," + it.latitude + ")");
  }

  return "https://api.mapbox.com/v4/mapbox.dark/${markers.joinToString(",")}/auto/800x800.png";
}

val loc = location()

val hospitals = queryCity("London")

val map = staticMap(loc!!, hospitals.rows.take(50).map { Location(it[1] as Double, it[2] as Double) })
show(map)
