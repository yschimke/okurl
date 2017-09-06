package com.baulsupp.oksocial.location

import java.util.Optional

class Location(val latitude: Double, val longitude: Double) {

    override fun toString(): String {
        return latitude.toString() + "," + longitude
    }

    companion object {

        fun latLong(latitude: Double, longitude: Double): Optional<Location> {
            return Optional.of(Location(latitude, longitude))
        }
    }
}
